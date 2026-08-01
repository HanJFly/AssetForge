package com.hjf.service;

import com.hjf.param.ReportAssetDetailParam;
import com.hjf.param.ReportLedgerSnapshotPageParam;
import com.hjf.param.ReportmonthlySummaryParam;
import com.hjf.vo.ReportAssetDetailVO;
import com.hjf.vo.ReportLedgerSnapshotPageVO;
import com.hjf.vo.ReportmonthlySummaryVO;

public interface IReportService {
    ReportAssetDetailVO detail(ReportAssetDetailParam param);

    ReportLedgerSnapshotPageVO ledgerSnapshotPage(ReportLedgerSnapshotPageParam param);

    ReportmonthlySummaryVO monthlySummary(ReportmonthlySummaryParam param);
}
