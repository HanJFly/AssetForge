package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TodoPageVo {
    private Long id;
    private String processNo;
    private String approvalType;
    private Long businessId;
    private String title;
    private String status;
    private Long applicantId;
    private String applicantName;
    private LocalDateTime createdAt;
}
