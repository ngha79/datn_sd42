package com.base.dto.request.conversation;

import com.base.entity.Message;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull
    private Long conversationId;

    @Size(max = 5000)
    private String messageContent;

    @Builder.Default
    private Message.MessageType messageType = Message.MessageType.TEXT;

    private String imageUrl;
}
