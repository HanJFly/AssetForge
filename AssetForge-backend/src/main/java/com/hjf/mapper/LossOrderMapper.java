package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.LossOrder;
import com.hjf.param.LossOrderPageParam;
import com.hjf.vo.LossOrderPageRecord;

import java.util.List;

/**
 * <p>
 * 盘亏单表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface LossOrderMapper extends BaseMapper<LossOrder> {

    List<LossOrderPageRecord> queryPage(LossOrderPageParam param);
}
