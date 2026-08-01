package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.param.ReportAssetDetailParam;
import com.hjf.param.ReportLedgerSnapshotPageParam;
import com.hjf.vo.ReportAssetDetailPageRecord;
import com.hjf.vo.ReportAssetDetailVO;
import com.hjf.vo.ReportLedgerSnapshotPageRecord;

import java.util.List;

public interface ReportMapper{
    List<ReportAssetDetailPageRecord> detail(ReportAssetDetailParam param);

    List<ReportLedgerSnapshotPageRecord> ledgerSnapshotPage(ReportLedgerSnapshotPageParam param);
}
