package com.hjf.vo;

import jdk.jfr.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ReportmonthlySummaryVO {
    private String snapshotMonth;
    private Long assetCount;
    private BigDecimal originalAmountTotal;
    private BigDecimal monthlyDepreciationTotal;
    private BigDecimal accumulatedDepreciationTotal;
    private BigDecimal netAmountTotal;
    private List<CategorySummaryList> categorySummary;
}
