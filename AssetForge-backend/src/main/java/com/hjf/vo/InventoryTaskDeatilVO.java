package com.hjf.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PrimitiveIterator;

@Data
public class InventoryTaskDeatilVO {
    private Long id;
    private String taskName;
    private String scopeType;
    private List<Long> scopeValue;
    private List<String> assetStatusFilter;
    private String status;
    private LocalDate deadLine;
    private Long responsibleUserId;
    private String conclusion;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
