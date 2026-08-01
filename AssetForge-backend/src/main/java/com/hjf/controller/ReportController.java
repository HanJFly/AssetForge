package com.hjf.controller;


import com.hjf.common.result.Result;
import com.hjf.param.ReportAssetDetailParam;
import com.hjf.param.ReportLedgerSnapshotPageParam;
import com.hjf.param.ReportmonthlySummaryParam;
import com.hjf.service.IReportService;
import com.hjf.vo.ReportAssetDetailVO;
import com.hjf.vo.ReportLedgerSnapshotPageVO;
import com.hjf.vo.ReportmonthlySummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private IReportService reportService;
    /*
    * 资产明细报表
    * */
    @PostMapping("/asset-detail")
    public Result<ReportAssetDetailVO> assetDetail(@RequestBody ReportAssetDetailParam param){
        ReportAssetDetailVO vo = reportService.detail(param);
        return Result.ok(vo);
    }

    /*
    * 月度快照分页
    * */
    @PostMapping(("/ledger-snapshot/page"))
    public Result<ReportLedgerSnapshotPageVO> ledgerSnapshotPage(@RequestBody ReportLedgerSnapshotPageParam param){
        ReportLedgerSnapshotPageVO vo = reportService.ledgerSnapshotPage(param);
        return Result.ok(vo);
    }
    /*
    * 月度汇总
    * */
    @PostMapping("/monthly-summary")
    public Result<ReportmonthlySummaryVO> monthlySummary(@RequestBody ReportmonthlySummaryParam param){
        ReportmonthlySummaryVO vo = reportService.monthlySummary(param);
        return Result.ok(vo);
    }

}
