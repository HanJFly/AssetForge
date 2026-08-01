package com.hjf.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryTaskPageRecord {
    private Long id;
    private String taskName;
    private String scopeType;
    private String status;
    private LocalDate deadLine;
    private Long responsibleUserId;
    private String conclusion;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
