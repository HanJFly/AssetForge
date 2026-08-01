package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.ReturnOrder;
import com.hjf.param.ReturnOrderPageParam;
import com.hjf.vo.ReturnOrderPageRecord;

import java.util.List;

/**
 * <p>
 * 归还单主表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ReturnOrderMapper extends BaseMapper<ReturnOrder> {


    List<ReturnOrderPageRecord> queryPage(ReturnOrderPageParam param);
}
