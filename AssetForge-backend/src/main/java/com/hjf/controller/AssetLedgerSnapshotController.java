package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.AssetLedgerSnapshotParam;
import com.hjf.service.IAssetLedgerSnapshotService;
import com.hjf.vo.AssetLedgerSnapshotPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 资产台账快照表（月度备份） 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/asset/ledger")
public class AssetLedgerSnapshotController {
    @Autowired
    private IAssetLedgerSnapshotService assetLedgerSnapshotService;

    @PostMapping("/page")
    public Result<AssetLedgerSnapshotPageVO> page(@RequestBody AssetLedgerSnapshotParam param) {
         AssetLedgerSnapshotPageVO page =assetLedgerSnapshotService.pageAssetLedgerSnapshot(param);

        return Result.ok(page);
    }

}
