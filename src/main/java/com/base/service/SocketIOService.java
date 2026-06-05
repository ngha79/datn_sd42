package com.base.service;

import com.base.repository.ConversationRepository;
import com.base.repository.UserRepository;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketIOService {

    private final SocketIOServer server;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    private final Map<Long, String> onlineUsers = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {

        server.addConnectListener(client -> {
            Long userId = getUserIdFromClient(client);
            if (userId != null) {
                onlineUsers.put(userId, client.getSessionId().toString());
                log.info("User {} connected", userId);

                // Tự động join staff-room nếu là STAFF hoặc ADMIN
                userRepository.findById(userId).ifPresent(user -> {
                    if (Objects.equals(user.getRole().getRoleName(), "STAFF") || Objects.equals(user.getRole().getRoleName(), "ADMIN")) {
                        client.joinRoom("staff-room");
                        log.info("User {} joined staff-room", userId);
                    }
                });
            }
        });

        server.addDisconnectListener(client -> {
            onlineUsers.entrySet().removeIf(
                    e -> e.getValue().equals(client.getSessionId().toString())
            );
        });

        // Join conversation room
        server.addEventListener("join_conversation", Long.class,
                (client, conversationId, ack) -> {
                    Long userId = getUserIdFromClient(client);
                    if (userId == null) return;

                    conversationRepository.findById(conversationId).ifPresent(conv -> {
                        boolean isUser = conv.getUser().getUserId().equals(userId);
                        boolean isStaff = conv.getStaff() != null
                                && conv.getStaff().getUserId().equals(userId);
                        boolean isAdmin = userRepository.findById(userId)
                                .map(u -> Objects.equals(u.getRole().getRoleName(), "ADMIN"))
                                .orElse(false);

                        if (isUser || isStaff || isAdmin) {
                            client.joinRoom("conversation-" + conversationId);
                            if (ack.isAckRequested()) ack.sendAckData("joined");
                        }
                    });
                });

        // Leave conversation room
        server.addEventListener("leave_conversation", Long.class,
                (client, conversationId, ack) -> {
                    client.leaveRoom("conversation-" + conversationId);
                });

        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    public void sendToUser(Long userId, String event, Object data) {
        String socketId = onlineUsers.get(userId);
        if (socketId != null) {
            SocketIOClient client = server.getClient(UUID.fromString(socketId));
            if (client != null) client.sendEvent(event, data);
        }
    }

    public void sendToRoom(String room, String event, Object data) {
        server.getRoomOperations(room).sendEvent(event, data);
    }

    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    private Long getUserIdFromClient(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        // Parse JWT lấy userId — inject JwtUtils
        return null;
    }
}
