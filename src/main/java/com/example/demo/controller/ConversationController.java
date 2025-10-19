package com.example.demo.controller;

import com.example.demo.dto.ChatMessageDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.ChatSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import com.example.demo.service.ConversationListSseService;
import java.util.Map;

@Controller
public class ConversationController {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageEntryRepository messageEntryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChatSseService chatSseService;

    @Autowired
    private ConversationListSseService conversationListSseService;

    /**
     * USER: View their own conversations, with optional filters by keyword and date.
     */
    @GetMapping("/my-conversations")
    @PreAuthorize("hasRole('USER')")
    public String myConversations(Model model,
                                  Principal principal,
                                  @RequestParam(value = "id", required = false) Long id,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Account user = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> conversations = conversationRepository.findByUser(user);

        // Apply filters
        if (date != null || (keyword != null && !keyword.isBlank())) {
            conversations = conversations.stream().filter(conv -> {
                boolean match = true;

                if (date != null) {
                    LocalDate lastUpdatedDate = conv.getLastUpdated().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    match &= lastUpdatedDate.equals(date);
                }

                if (keyword != null && !keyword.isBlank()) {
                    List<MessageEntry> messages = messageEntryRepository.findByConversationOrderBySentAtAsc(conv);
                    match &= messages.stream().anyMatch(msg ->
                            msg.getContent() != null &&
                                    msg.getContent().toLowerCase().contains(keyword.toLowerCase()));
                }
                return match;
            }).collect(Collectors.toList());
        }

        // If a specific conversation is requested, load its messages
        if (id != null) {
            conversationRepository.findById(id).ifPresent(conv -> {
                model.addAttribute("messages",
                        messageEntryRepository.findByConversationOrderBySentAtAsc(conv));
            });
            model.addAttribute("activeId", id);
        }

        model.addAttribute("conversations", conversations);
        return "user/conversations";
    }

    /**
     * View a single conversation with all its messages.
     */
    @GetMapping("/conversation/{id}")
    @PreAuthorize("isAuthenticated()")
    public String viewConversation(@PathVariable Long id, Model model, Principal principal, @RequestHeader(value = "referer", required = false) String referer) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        List<MessageEntry> messages = messageEntryRepository
                .findByConversationOrderBySentAtAsc(conversation);

        MessageEntry newMessage = new MessageEntry();
        newMessage.setConversation(conversation);

// mark unread messages as read when admin views conversation
        if (principal != null && principal.getName().equalsIgnoreCase("admin")) {
            List<MessageEntry> unreads = messageEntryRepository
                    .findByConversationAndFromAdminFalseAndReadByAdminFalse(conversation);
            if (!unreads.isEmpty()) {
                unreads.forEach(m -> m.setReadByAdmin(true));
                messageEntryRepository.saveAll(unreads);
            }
        }
        model.addAttribute("conversation", conversation);
        model.addAttribute("messages", messages);
        model.addAttribute("newMessage", newMessage);

        return "conversation/view";
    }

    /**
     * Send a new message to a conversation (admin or user).
     */
    @PostMapping("/conversation/{id}/send")
    @PreAuthorize("isAuthenticated()")
    public String sendMessage(@PathVariable Long id,
                              @ModelAttribute("newMessage") MessageEntry newMessage,
                              @RequestParam(value = "text", required = false) String text,
                              Principal principal, @RequestHeader(value = "referer", required = false) String referer) {

        Optional<Account> optionalAccount = accountRepository.findByUsername(principal.getName());
        // Get sender details
        Account sender = optionalAccount.orElseGet(() -> {
            if ("admin".equalsIgnoreCase(principal.getName())) {
                Account adminFallback = new Account();
                adminFallback.setUsername("admin");
                adminFallback.setRole("ROLE_ADMIN");
                return adminFallback;
            }
            throw new RuntimeException("User not found");
        });
        // Ensure message content is populated (supports plain "text" form field)
        if ((newMessage.getContent() == null || newMessage.getContent().isBlank()) && text != null) {
            newMessage.setContent(text.trim());
        }
        if (newMessage.getContent() == null || newMessage.getContent().isBlank()) {
            // Nothing to send – redirect back without saving
            return buildRedirect(referer, id);
        }

        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Prepare message
        newMessage.setSender(sender);
        newMessage.setConversation(conversation);
        newMessage.setSentAt(new Date());
        newMessage.setFromAdmin(sender.getRole() != null && sender.getRole().contains("ADMIN"));
        newMessage.setReadByAdmin(sender.getRole() != null && sender.getRole().contains("ADMIN"));

        // Save and broadcast
        MessageEntry savedMessage = messageEntryRepository.save(newMessage);

        ChatMessageDTO dto = new ChatMessageDTO(
                savedMessage.getId(),
                sender.getUsername(),
                savedMessage.getContent(),
                java.time.LocalDateTime.now(),
                savedMessage.isFromAdmin()
        );

        chatSseService.send(id, dto);

        conversation.setLastUpdated(new Date());
        conversationRepository.save(conversation);
        conversationListSseService.send(conversation);

        if (sender.getRole() != null && sender.getRole().contains("ADMIN")) {
            return "redirect:/admin/conversations?id=" + id + "&success";
        } else {
            return buildRedirect(referer, id);
        }
    }

    /**
     * Start a new conversation from the user side.
     */
    @PostMapping("/start-conversation")
    @PreAuthorize("hasRole('USER')")
    public String startConversation(@RequestParam("message") String messageContent,
                                    Model model,
                                    Principal principal, @RequestHeader(value = "referer", required = false) String referer) {
        Account user = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setLastUpdated(new Date());
        conversationRepository.save(conversation);
        conversationListSseService.send(conversation);

        MessageEntry message = new MessageEntry();
        message.setConversation(conversation);
        message.setSender(user);
        message.setContent(messageContent);
        message.setSentAt(new Date());
        message.setFromAdmin(false);
        messageEntryRepository.save(message);

        return "redirect:/my-conversations?success";
    }

    /**
     * ADMIN: View all conversations with filters and selected thread preview.
     */
    @GetMapping("/admin/conversations")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewAllConversations(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "id", required = false) Long id,
            Model model) {

        List<Conversation> conversations = conversationRepository.findAll();
// Build map of unread user messages per conversation
        Map<Long, Long> unreadMap = conversations.stream()
                .collect(Collectors.toMap(
                        Conversation::getId,
                        c -> messageEntryRepository.countByConversationAndFromAdminFalseAndReadByAdminFalse(c)
                ));
        model.addAttribute("unreadMap", unreadMap);


        // Show messages of selected conversation
        if (id != null) {
            conversationRepository.findById(id).ifPresent(conv -> {
                model.addAttribute("messages",
                        messageEntryRepository.findByConversationOrderBySentAtAsc(conv));
            });
            model.addAttribute("activeId", id);
        }
        // Apply filters
        if (username != null && !username.isBlank()) {
            conversations.removeIf(conv -> conv.getUser() == null ||
                    !conv.getUser().getUsername().toLowerCase().contains(username.toLowerCase()));
        }

        if (date != null && !date.isBlank()) {
            conversations.removeIf(conv -> {
                String formatted = new java.text.SimpleDateFormat("yyyy-MM-dd").format(conv.getLastUpdated());
                return !formatted.equals(date);
            });
        }

        if (keyword != null && !keyword.isBlank()) {
            conversations.removeIf(conv -> {
                List<MessageEntry> messages = messageEntryRepository.findByConversationOrderBySentAtAsc(conv);
                return messages.stream().noneMatch(msg ->
                        msg.getContent() != null &&
                                msg.getContent().toLowerCase().contains(keyword.toLowerCase()));
            });
        }

        model.addAttribute("conversations", conversations);
        return "admin/conversations";
    }

    /**
     * SSE: Provide real-time message updates for a specific conversation.
     */
    @GetMapping("/conversation/{id}/stream")
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream(@PathVariable Long id) {
        return chatSseService.register(id);
    }


    /**
     * USER: Auto-create and redirect to a conversation (used for popup chat).
     */

    @GetMapping("/conversation")
    @PreAuthorize("hasRole('USER')")
    public String userConversationRedirect(Principal principal, @RequestHeader(value = "referer", required = false) String referer) {
        Account user = accountRepository.findByUsername(principal.getName())
                .orElseGet(() -> accountRepository.findByEmail(principal.getName()).orElse(null));
        if (user == null) {
            return "redirect:/";
        }
        Conversation convo = new Conversation();
        convo.setUser(user);
        convo.setLastUpdated(new java.util.Date());
        conversationRepository.save(convo);
        conversationListSseService.send(convo);
        return "redirect:/conversation/" + convo.getId();
    }


    /**
     * ADMIN: Delete a conversation and all its messages.
     */
    @PostMapping("/admin/conversations/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteConversation(@PathVariable Long id) {
        conversationRepository.findById(id).ifPresent(conv -> {
            messageEntryRepository.deleteAll(messageEntryRepository.findByConversationOrderBySentAtAsc(conv));
            conversationRepository.delete(conv);
        });
        return "redirect:/admin/conversations?deleted";
    }

    /**
     * REST API: Returns the ID of the user's current conversation, or creates one if needed.
     */
    @GetMapping("/api/my-conversation-id")
    @PreAuthorize("hasRole('USER')")
    @ResponseBody
    public Long getMyConversationId(HttpSession session, Principal principal, @RequestHeader(value = "referer", required = false) String referer) {
        Long existing = (Long) session.getAttribute("conversationId");
        if (existing != null) {
            return existing;
        }
        Account user = accountRepository.findByUsername(principal.getName())
                .orElseGet(() -> accountRepository.findByEmail(principal.getName()).orElse(null));
        if (user == null) {
            return null;
        }
        Conversation conv = new Conversation();
        conv.setUser(user);
        conv.setLastUpdated(new java.util.Date());
        conversationRepository.save(conv);
        conversationListSseService.send(conv);
        session.setAttribute("conversationId", conv.getId());
        return conv.getId();
    }


    /**
     * USER or ADMIN: Delete a conversation from the user side (only if owner or admin).
     */
    @PostMapping("/conversation/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String deleteConversationUser(@PathVariable Long id, Principal principal, @RequestHeader(value = "referer", required = false) String referer) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        // Only the owner or admin may delete
        boolean isOwner = conv.getUser() != null && conv.getUser().getUsername().equals(principal.getName());
        boolean isAdmin = principal.getName().equalsIgnoreCase("admin");
        if (!isOwner && !isAdmin) {
            return "redirect:/?unauthorized";
        }
        messageEntryRepository.deleteAll(messageEntryRepository.findByConversationOrderBySentAtAsc(conv));
        conversationRepository.delete(conv);
        return isAdmin ? "redirect:/admin/conversations?deleted" : "redirect:/my-conversations?deleted";
    }


    /**
     * Decide where to redirect after sending a message based on the page the request originated from.
     */
    private String buildRedirect(String referer, Long conversationId) {
        if (referer != null && referer.contains("/my-conversations")) {
            // stay on My Conversations page, preserving query parameters
            if (referer.contains("?")) {
                return "redirect:" + referer + "&success";
            } else {
                return "redirect:" + referer + "?success";
            }
        }
        // fall back to single‑thread view
        return "redirect:/conversation/" + conversationId + "?success";
    }
}