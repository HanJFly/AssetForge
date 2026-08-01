package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.AssetLedgerSnapshot;
import com.hjf.param.AssetLedgerSnapshotParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 资产台账快照表（月度备份） Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface AssetLedgerSnapshotMapper extends BaseMapper<AssetLedgerSnapshot> {

    List<AssetLedgerSnapshot> queryPage( @Param("param") AssetLedgerSnapshotParam param);

}
