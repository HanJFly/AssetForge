package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReturnOrderPageRecord {
    private Long id;
    private String orderNo;
    private String returnUserName;
    private String approvalStatus;
    private LocalDateTime createdAt;
}
