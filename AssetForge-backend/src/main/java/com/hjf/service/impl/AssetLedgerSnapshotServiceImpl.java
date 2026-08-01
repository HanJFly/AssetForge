package com.hjf.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hjf.entity.AssetLedgerSnapshot;
import com.hjf.mapper.AssetLedgerSnapshotMapper;
import com.hjf.param.AssetLedgerSnapshotParam;
import com.hjf.service.IAssetLedgerSnapshotService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.hjf.vo.AssetLedgerSnapshotPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 资产台账快照表（月度备份） 服务实现类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@Service
public class AssetLedgerSnapshotServiceImpl extends ServiceImpl<AssetLedgerSnapshotMapper, AssetLedgerSnapshot> implements IAssetLedgerSnapshotService {

    @Autowired
    private AssetLedgerSnapshotMapper assetLedgerSnapshotMapper;
    @Override
    public AssetLedgerSnapshotPageVO pageAssetLedgerSnapshot(AssetLedgerSnapshotParam param) {
        PageHelper.startPage(param.getPage(), param.getPageSize());
        //查询
        List<AssetLedgerSnapshot> list = assetLedgerSnapshotMapper.queryPage(param);
        //从PageHelper提取分页信息
        Page<AssetLedgerSnapshot> pageInfo = (Page<AssetLedgerSnapshot>) list;

        AssetLedgerSnapshotPageVO vo =new AssetLedgerSnapshotPageVO();

       vo.setRecords(pageInfo);
       vo.setTotal(pageInfo.getTotal());
       vo.setPage(pageInfo.getPageNum());
       vo.setSize(pageInfo.getPageSize());
        return vo;
    }
}
