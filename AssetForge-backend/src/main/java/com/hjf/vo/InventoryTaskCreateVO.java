package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryTaskCreateVO {
    private Long id;
    private String status;
    private Integer detailCount;
    private LocalDateTime createdAt;
}
