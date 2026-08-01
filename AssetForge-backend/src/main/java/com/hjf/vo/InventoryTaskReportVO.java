package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryTaskReportVO {
    private Long taskId;
    private String taskName;
    private Integer totalCount;
    private Integer normalCount;
    private Integer lossCount;
    private Integer gainCount;
    private Integer mismatchCount;
    private BigDecimal lossRate;
}
