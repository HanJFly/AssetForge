package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportLedgerSnapshotPageRecord {
    private Long id;
    private String snapshotMonth;
    private String assetCode;
    private String assetName;
    private BigDecimal originalAmount;
    private BigDecimal monthlyDepreciation;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netAmount;
}
