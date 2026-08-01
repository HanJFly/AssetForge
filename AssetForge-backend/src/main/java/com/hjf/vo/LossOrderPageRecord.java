package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LossOrderPageRecord {
    private Long id;
    private String orderNo;
    private String assetCode;
    private String assetName;
    private String compensationStatus;
    private LocalDateTime createdAt;

}
