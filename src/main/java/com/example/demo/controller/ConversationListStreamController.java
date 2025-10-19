package com.example.demo.controller;

import com.example.demo.service.ConversationListSseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller that provides a Server-Sent Events (SSE) endpoint for streaming
 * newly created conversations to the admin interface in real-time.
 *
 * This is used to dynamically update the admin's conversation list view
 * without requiring page refreshes.
 */
@RestController
@RequestMapping("/admin/conversations")
@PreAuthorize("hasRole('ADMIN')")
public class ConversationListStreamController {

    private final ConversationListSseService listSseService;

    // Constructor injection for the SSE service that manages emitters
    public ConversationListStreamController(ConversationListSseService listSseService) {
        this.listSseService = listSseService;
    }

    /**
     * Endpoint that returns an SseEmitter to the client.
     * This emitter will push events (new conversations) to the client as they occur.
     *
     * @return an active SseEmitter for real-time communication
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        return listSseService.addEmitter();
    }
}