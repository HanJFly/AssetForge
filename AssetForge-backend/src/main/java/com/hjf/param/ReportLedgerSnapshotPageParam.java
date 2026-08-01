package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class ReportLedgerSnapshotPageParam extends Page {
    private String snapshotMonth;
    private Long categoryId;
    private Long departmentId;
}
