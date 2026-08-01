package com.hjf.param;

import lombok.Data;

@Data
public class FileAttachmentBindParam {
    private Long bizId;
    private String bizType;
    private Long attachmentIds;
}
