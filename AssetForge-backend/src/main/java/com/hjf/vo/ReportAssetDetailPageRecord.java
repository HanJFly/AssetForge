package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportAssetDetailPageRecord {
    private Long assetId;
    private String assetCode;
    private String assetName;
    private String categoryName;
    private String departmentName;
    private String userName;
    private String status;
    private BigDecimal purchasePrice;  //购置金额
    private BigDecimal netAmount;  // 现值
}
