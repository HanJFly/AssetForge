package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategorySummaryList {
    private Long categoryId;
    private String categoryName;
    private Long assetCount;
    private BigDecimal originalAmountTotal;
    private BigDecimal netAmountTotal;
}
