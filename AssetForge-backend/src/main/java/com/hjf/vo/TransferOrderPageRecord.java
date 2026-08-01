package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransferOrderPageRecord {
    private Long id;
    private String orderNo;
    private String fromUserDepartmentName;
    private String toUserDepartmentName;
    private String approvalStatus;
    private LocalDateTime createdAt;
}
