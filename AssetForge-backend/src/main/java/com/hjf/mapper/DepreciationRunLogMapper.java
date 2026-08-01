package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.DepreciationRunLog;
import com.hjf.param.DepreciationRunLogPageParam;
import com.hjf.vo.DepreciationRunLogPageRecord;

import java.util.List;

/**
 * <p>
 * 月度折旧执行记录表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface DepreciationRunLogMapper extends BaseMapper<DepreciationRunLog> {

    List<DepreciationRunLogPageRecord> queryPage(DepreciationRunLogPageParam param);
}
