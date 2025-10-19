package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE hub that pushes newly created conversations to all open admin dashboards.
 * Manages registration of emitters and broadcasting events when new conversations are created.
 */
@Service
public class ConversationListSseService {

    /**
     * Thread-safe list holding all active emitters subscribed to conversation updates.
     */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registers a new SseEmitter and keeps it alive indefinitely for admin dashboards.
     * Cleans up emitters when clients disconnect or time out.
     *
     * @return A new SseEmitter instance.
     */
    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    /**
     * Broadcasts a payload to all currently connected emitters.
     * Automatically removes any dead emitters.
     *
     * @param payload The data to send as an SSE event.
     */
    public void send(Object payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("conversation").data(payload));
            } catch (IOException ex) {
                emitters.remove(emitter); // Remove dead connection
            }
        }
    }

    /**
     * Listens for ConversationCreatedEvent after a transaction commits and triggers broadcasting.
     *
     * @param ev The event containing the new conversation data.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConversationCreated(com.example.demo.events.ConversationCreatedEvent ev) {
        send(ev.getConversation());
    }
}
