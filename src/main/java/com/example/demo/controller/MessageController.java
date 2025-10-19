package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.Message;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;

@Controller
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Displays the contact form for users to send messages to the admin.
     */
    @GetMapping("/contact")
    public String showContactForm(Model model) {
        model.addAttribute("message", new Message());
        return "contact";
    }

    /**
     * Handles message submission from the contact form.
     * The sender is automatically determined based on the logged-in user.
     */
    @PostMapping("/contact")
    public String sendMessage(@ModelAttribute Message message, Principal principal) {
        Account sender = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        message.setSender(sender);
        message.setSentAt(new Date());
        message.setLastUpdated(new Date());
        messageRepository.save(message);
        return "redirect:/contact?success";// Redirect with success indicator
    }

    /**
     * Admin view: displays all messages received from users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/messages")
    public String viewMessages(Model model) {
        model.addAttribute("messages", messageRepository.findAll());
        return "admin/messages";
    }

    /**
     * Admin action: mark a message as answered.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/messages/{id}/answer")
    public String markMessageAsAnswered(@PathVariable Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setAnswered(true);
        messageRepository.save(message);
        return "redirect:/admin/messages";
    }

    /**
     * Admin action: reply to a specific user message.
     * Sets the reply content and marks the message as answered.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/messages/{id}/reply")
    public String replyToMessage(@PathVariable Long id, @RequestParam("reply") String reply) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setReply(reply);
        message.setAnswered(true);
        message.setLastUpdated(new Date());
        messageRepository.save(message);

        return "redirect:/admin/messages";
    }

    /**
     * User view: allows a user to view only their own messages.
     */
    @GetMapping("/my-messages")
    @PreAuthorize("hasRole('USER')")
    public String viewUserMessages(Model model, Principal principal) {
        Account user = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("userMessages", messageRepository.findBySender(user));
        return "user/messages"; // must match template path
    }



    /**
     * User action: sends a follow-up reply to a previously submitted message.
     * Ensures that users can only reply to their own messages.
     */
    @PostMapping("/my-messages/{id}/reply")
    @PreAuthorize("hasRole('USER')")
    public String replyToAdmin(@PathVariable Long id,
                               @RequestParam("followUp") String followUp,
                               Principal principal) {

        Account user = accountRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Prevent users from editing others' messages
        if (!message.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied.");
        }

        message.setCustomerFollowUp(followUp);
        message.setLastUpdated(new Date());
        messageRepository.save(message);

        return "redirect:/my-messages";
    }
}
