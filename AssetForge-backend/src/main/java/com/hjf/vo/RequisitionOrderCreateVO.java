package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequisitionOrderCreateVO {
    private Long id;
    private String orderNo;
    private String approvalStatus;
    private LocalDateTime createTime;
}
