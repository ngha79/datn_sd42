package com.base.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadPostMessage implements Serializable {

    private Long postId;
    private String tempFilePath;
    private String oldImageUrl;
    private ActionType action;

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE
    }
}
