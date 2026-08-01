package com.hjf.vo;

import lombok.Data;

@Data
public class DepreciationRunLogExecuteVO {
    private Long id;
    private String runMonth;
    private Integer processedCount;
    private String status;
}
