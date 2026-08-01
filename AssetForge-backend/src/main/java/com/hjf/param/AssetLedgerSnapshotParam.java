package com.hjf.param;

import com.hjf.entity.AssetLedgerSnapshot;
import lombok.Data;

@Data
public class AssetLedgerSnapshotParam extends AssetLedgerSnapshot {
    int page = 1; // 当前页码
    int pageSize = 10; // 每页显示的记录数
    private Long categoryId;
    private Long departmentId;
}
