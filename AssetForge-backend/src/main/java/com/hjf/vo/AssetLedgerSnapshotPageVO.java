package com.hjf.vo;

import com.hjf.entity.AssetLedgerSnapshot;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AssetLedgerSnapshotPageVO{
    private List<AssetLedgerSnapshot> records = new ArrayList<>();
    private Long total;
    private Integer page;
    private Integer size;
}
