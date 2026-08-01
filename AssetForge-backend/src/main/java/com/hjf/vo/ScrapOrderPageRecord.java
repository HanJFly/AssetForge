package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScrapOrderPageRecord {
    private Long id;
    private String orderNo;
    private String applicantName;
    private String approvalStatus;
    private LocalDateTime createdAt;
}
