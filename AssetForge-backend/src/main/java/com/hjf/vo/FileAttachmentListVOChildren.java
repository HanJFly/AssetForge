package com.hjf.vo;

import lombok.Data;

@Data
public class FileAttachmentListVOChildren {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
}
