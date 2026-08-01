package com.hjf.service;

import com.hjf.entity.AssetLedgerSnapshot;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.AssetLedgerSnapshotParam;
import com.hjf.vo.AssetLedgerSnapshotPageVO;

/**
 * <p>
 * 资产台账快照表（月度备份） 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IAssetLedgerSnapshotService extends IService<AssetLedgerSnapshot> {

    AssetLedgerSnapshotPageVO pageAssetLedgerSnapshot(AssetLedgerSnapshotParam param);
}
