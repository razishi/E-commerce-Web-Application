package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service class for managing Server-Sent Events (SSE) emitters related to chat conversations.
 * Handles registration of emitters and broadcasting chat messages in real time.
 */
@Service
public class ChatSseService {

    /**
     * Stores lists of SseEmitters mapped by conversation ID.
     * Uses thread-safe collections to handle concurrent access.
     */
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Registers a new SseEmitter for a specific conversation ID.
     * This allows the client to receive real-time chat updates.
     *
     * @param conversationId The conversation ID to listen for.
     * @return A new SseEmitter instance.
     */
    public SseEmitter register(Long conversationId) {
        SseEmitter emitter = new SseEmitter(0L); // Never timeout
        emitters.computeIfAbsent(conversationId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() ->
                emitters.getOrDefault(conversationId, List.of()).remove(emitter));
        emitter.onTimeout(() ->
                emitters.getOrDefault(conversationId, List.of()).remove(emitter));

        return emitter;
    }

    /**
     * Sends data to all registered emitters for a specific conversation ID.
     * Automatically removes any emitters that fail to send (e.g., disconnected clients).
     *
     * @param conversationId The conversation ID to broadcast to.
     * @param data           The data object to send as an SSE event.
     */
    public void send(Long conversationId, Object data) {
        List<SseEmitter> list = emitters.get(conversationId);
        if (list == null) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("chat").data(data));
            } catch (IOException ex) {
                list.remove(emitter); // Remove dead connection
            }
        }
    }
}
