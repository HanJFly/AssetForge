package com.hjf.vo;

import lombok.Data;

import java.util.List;

@Data
public class ReportLedgerSnapshotPageVO {
    private List<ReportLedgerSnapshotPageRecord> records;
    private Long total;
    private Integer page;
    private Integer Size;
}
