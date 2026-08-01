package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepreciationRunLogPageRecord {
    private Long id;
    private String runMonth;
    private Integer processedCount;
    private Integer skippedCount;
    private BigDecimal totalMonthlyDepreciation;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private String errorMessage;
}
