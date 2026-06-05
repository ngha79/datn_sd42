package com.base.service.impl;

import com.base.dto.request.ImageUploadBannerMessage;
import com.base.dto.request.ImageUploadChatMessage;
import com.base.dto.request.conversation.CreateConversationRequest;
import com.base.dto.request.conversation.SendMessageRequest;
import com.base.dto.response.conversation.ConversationResponse;
import com.base.dto.response.conversation.MessageResponse;
import com.base.entity.*;
import com.base.exception.BadRequestException;
import com.base.exception.ForbiddenException;
import com.base.exception.ResourceNotFoundException;
import com.base.queue.ImageUploadProducer;
import com.base.repository.ConversationRepository;
import com.base.repository.MessageRepository;
import com.base.repository.UserRepository;
import com.base.service.ChatService;
import com.base.service.SocketIOService;
import com.base.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final SocketIOService socketIOService;
    private final LocalStorageService localStorageService;
    private final ImageUploadProducer imageUploadProducer;

    @Override
    public ConversationResponse createConversation(CreateConversationRequest request, MultipartFile file) {
        Long userId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean hasActive = conversationRepository.existsByUser_UserIdAndStatusIn(
                userId,
                List.of(
                        Conversation.ConversationStatus.OPEN,
                        Conversation.ConversationStatus.PENDING
                )
        );
        if (hasActive) {
            throw new BadRequestException(
                    "Bạn đang có cuộc hội thoại chưa kết thúc. " +
                            "Vui lòng đóng trước khi tạo mới."
            );
        }

        Conversation conversation = Conversation.builder()
                .user(user)
                .title(request.getTitle() != null && !request.getTitle().isBlank()
                        ? request.getTitle()
                        : "Tư vấn #" + userId)
                .status(Conversation.ConversationStatus.PENDING)
                .build();

        conversation = conversationRepository.save(conversation);
        System.out.println("msg"+ request.getFirstMessage());
        if (request.getFirstMessage() != null && !request.getFirstMessage().isBlank()) {
            Message message = Message.builder()
                    .conversation(conversation)
                    .sender(user)
                    .messageContent(request.getFirstMessage())
                    .messageType(Message.MessageType.TEXT)
                    .build();

            if (file != null && !file.isEmpty()) {
                String tempPath = localStorageService.saveTempFile(file);
                String tempUrl = localStorageService.getTempUrl(tempPath);

                message.setImageUrl(tempUrl);

                messageRepository.save(message);

                imageUploadProducer.sendUploadChatMessage(
                        ImageUploadChatMessage.builder()
                                .messageId(message.getMessageId())
                                .tempFilePath(tempPath)
                                .action(ImageUploadChatMessage.ActionType.CREATE)
                                .build()
                );
            } else {
                messageRepository.save(message);
            }
            conversation.updateLastMessage(request.getFirstMessage(), true);
            conversationRepository.save(conversation);
        }

        socketIOService.sendToRoom("staff-room", "new_conversation",
                toConversationResponse(conversation));

        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long conversationId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        validateParticipant(conversation, currentUserId);

        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getMyConversations(
            Conversation.ConversationStatus status, Pageable pageable
    ) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return switch (currentUser.getRole().getRoleName()) {

            case "USER" -> status != null
                    ? conversationRepository
                    .findByUser_UserIdAndStatusOrderByLastMessageAtDesc(
                            currentUserId, status, pageable)
                    .map(this::toConversationResponse)
                    : conversationRepository
                    .findByUser_UserIdOrderByLastMessageAtDesc(
                            currentUserId, pageable)
                    .map(this::toConversationResponse);

            case "STAFF" -> status != null
                    ? conversationRepository
                    .findByStaff_UserIdAndStatusOrderByLastMessageAtDesc(
                            currentUserId, status, pageable)
                    .map(this::toConversationResponse)
                    : conversationRepository
                    .findByStaff_UserIdOrderByLastMessageAtDesc(
                            currentUserId, pageable)
                    .map(this::toConversationResponse);

            default -> throw new ForbiddenException("Không có quyền truy cập");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getPendingConversations(Pageable pageable) {
        return conversationRepository
                .findByStaffIsNullAndStatusOrderByCreatedAtAsc(
                        Conversation.ConversationStatus.PENDING, pageable)
                .map(this::toConversationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponse> getAllConversations(
            Conversation.ConversationStatus status, Pageable pageable
    ) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!hasRole(currentUser, "ADMIN")) {
            throw new ForbiddenException("Chỉ admin mới có quyền xem tất cả");
        }

        return status != null
                ? conversationRepository
                .findByStatusOrderByLastMessageAtDesc(status, pageable)
                .map(this::toConversationResponse)
                : conversationRepository
                .findAllByOrderByLastMessageAtDesc(pageable)
                .map(this::toConversationResponse);
    }

    @Override
    public ConversationResponse assignStaff(Long conversationId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!hasRole(currentUser, "STAFF") && !hasRole(currentUser, "ADMIN")) {
            throw new ForbiddenException("Chỉ staff mới được tiếp nhận conversation");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new BadRequestException("Không thể tiếp nhận conversation đã đóng");
        }

        if (conversation.getStaff() != null && !hasRole(currentUser, "ADMIN")) {
            throw new BadRequestException("Conversation đã được tiếp nhận bởi staff khác");
        }

        conversation.setStaff(currentUser);
        conversation.setStatus(Conversation.ConversationStatus.OPEN);
        conversation = conversationRepository.save(conversation);

        socketIOService.sendToUser(
                conversation.getUser().getUserId(),
                "staff_joined",
                toConversationResponse(conversation)
        );

        return toConversationResponse(conversation);
    }

    @Override
    public ConversationResponse reassignStaff(Long conversationId, Long newStaffId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!hasRole(currentUser, "ADMIN")) {
            throw new ForbiddenException("Chỉ admin mới được reassign staff");
        }

        User newStaff = userRepository.findById(newStaffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        if (!hasRole(newStaff, "STAFF")) {
            throw new BadRequestException("User được chỉ định không phải staff");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new BadRequestException("Không thể reassign conversation đã đóng");
        }

        conversation.setStaff(newStaff);
        conversation = conversationRepository.save(conversation);

        socketIOService.sendToRoom(
                "conversation-" + conversationId,
                "staff_reassigned",
                toConversationResponse(conversation)
        );

        return toConversationResponse(conversation);
    }

    @Override
    public ConversationResponse closeConversation(Long conversationId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        boolean isOwner = conversation.getUser().getUserId().equals(currentUserId);
        boolean isStaff = conversation.getStaff() != null
                && conversation.getStaff().getUserId().equals(currentUserId);
        boolean isAdmin = hasRole(currentUser, "ADMIN");

        if (!isOwner && !isStaff && !isAdmin) {
            throw new ForbiddenException("Bạn không có quyền đóng conversation này");
        }

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new BadRequestException("Conversation đã đóng rồi");
        }

        conversation.setStatus(Conversation.ConversationStatus.CLOSED);
        conversation = conversationRepository.save(conversation);

        socketIOService.sendToRoom(
                "conversation-" + conversationId,
                "conversation_closed",
                toConversationResponse(conversation)
        );

        return toConversationResponse(conversation);
    }

    @Override
    public MessageResponse sendMessage(SendMessageRequest request, MultipartFile file) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (hasRole(sender, "ADMIN")) {
            throw new ForbiddenException("Admin không thể gửi tin nhắn");
        }

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        validateParticipant(conversation, currentUserId);

        if (conversation.getStatus() == Conversation.ConversationStatus.CLOSED) {
            throw new BadRequestException("Conversation đã đóng, không thể gửi tin nhắn");
        }

        if (request.getMessageType() == Message.MessageType.TEXT
                && (request.getMessageContent() == null
                || request.getMessageContent().isBlank())) {
            throw new BadRequestException("Nội dung tin nhắn không được để trống");
        }

        if (request.getMessageType() == Message.MessageType.IMAGE
                && (request.getImageUrl() == null || request.getImageUrl().isBlank())) {
            throw new BadRequestException("URL ảnh không được để trống");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageContent(request.getMessageContent())
                .messageType(request.getMessageType())
                .imageUrl(null)
                .build();

        if (file != null && !file.isEmpty()) {
            String tempPath = localStorageService.saveTempFile(file);
            String tempUrl = localStorageService.getTempUrl(tempPath);

            message.setImageUrl(tempUrl);

            message = messageRepository.save(message);

            imageUploadProducer.sendUploadChatMessage(
                    ImageUploadChatMessage.builder()
                            .messageId(message.getMessageId())
                            .tempFilePath(tempPath)
                            .action(ImageUploadChatMessage.ActionType.CREATE)
                            .build()
            );
        } else {
            message = messageRepository.save(message);
        }

        boolean sentByUser = conversation.getUser().getUserId().equals(currentUserId);
        String preview = request.getMessageType() == Message.MessageType.IMAGE
                ? "[Hình ảnh]"
                : request.getMessageContent();

        conversation.updateLastMessage(preview, sentByUser);
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(message);

        socketIOService.sendToRoom(
                "conversation-" + conversation.getConversationId(),
                "new_message",
                response
        );

        Long recipientId = sentByUser
                ? (conversation.getStaff() != null
                ? conversation.getStaff().getUserId() : null)
                : conversation.getUser().getUserId();

        if (recipientId != null && !socketIOService.isOnline(recipientId)) {
            log.info("User {} offline — push notification pending", recipientId);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long conversationId, Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        validateParticipant(conversation, currentUserId);

        return messageRepository
                .findByConversation_ConversationIdAndDeletedFalseOrderBySentAtAsc(
                        conversationId, pageable)
                .map(this::toMessageResponse);
    }

    @Override
    public void markAsRead(Long conversationId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fix: dùng Objects.equals thay vì ==
        if (hasRole(currentUser, "ADMIN")) {
            throw new ForbiddenException("Admin không cần đánh dấu đã đọc");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        validateParticipant(conversation, currentUserId);

        messageRepository.markAllAsRead(conversationId, currentUserId, LocalDateTime.now());

        boolean isUser = conversation.getUser().getUserId().equals(currentUserId);
        if (isUser) {
            conversation.setUnreadCountUser(0);
        } else {
            conversation.setUnreadCountStaff(0);
        }
        conversationRepository.save(conversation);

        socketIOService.sendToRoom(
                "conversation-" + conversationId,
                "messages_read",
                Map.of("conversationId", conversationId, "readBy", currentUserId)
        );
    }

    @Override
    public void deleteMessage(Long messageId) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getUserId().equals(currentUserId)) {
            throw new ForbiddenException("Bạn không có quyền xóa tin nhắn này");
        }

        if (message.getSentAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Chỉ có thể xóa tin nhắn trong vòng 5 phút");
        }

        message.setDeleted(true);
        messageRepository.save(message);

        socketIOService.sendToRoom(
                "conversation-" + message.getConversation().getConversationId(),
                "message_deleted",
                Map.of("messageId", messageId)
        );
    }

    private boolean hasRole(User user, String roleName) {
        return Objects.equals(user.getRole().getRoleName(), roleName);
    }

    private void validateParticipant(Conversation conversation, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (hasRole(user, "ADMIN")) return;

        boolean isOwner = conversation.getUser().getUserId().equals(userId);
        boolean isStaff = conversation.getStaff() != null
                && conversation.getStaff().getUserId().equals(userId);

        if (!isOwner && !isStaff) {
            throw new ForbiddenException("Bạn không có quyền truy cập conversation này");
        }
    }

    private MessageResponse toMessageResponse(Message message) {
        return MessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getUsername())
                .senderAvatar(message.getSender().getAvatar())
                .messageContent(message.isDeleted()
                        ? "[Tin nhắn đã bị xóa]"
                        : message.getMessageContent())
                .imageUrl(message.getImageUrl())
                .messageType(message.getMessageType())
                .isRead(message.isRead())
                .readAt(message.getReadAt())
                .sentAt(message.getSentAt())
                .deleted(message.isDeleted())
                .build();
    }

    private ConversationResponse toConversationResponse(Conversation c) {
        return ConversationResponse.builder()
                .conversationId(c.getConversationId())
                .title(c.getTitle())
                .userId(c.getUser().getUserId())
                .userName(c.getUser().getUsername())
                .staffId(c.getStaff() != null ? c.getStaff().getUserId() : null)
                .staffName(c.getStaff() != null ? c.getStaff().getUsername() : null)
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .unreadCountUser(c.getUnreadCountUser())
                .unreadCountStaff(c.getUnreadCountStaff())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
