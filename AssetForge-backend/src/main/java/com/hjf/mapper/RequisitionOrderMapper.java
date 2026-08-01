package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.RequisitionOrder;
import com.hjf.param.RequisitionOrderPageParam;
import com.hjf.vo.RequisitionOrderPageRecord;

import java.util.List;

/**
 * <p>
 * 申领单主表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface RequisitionOrderMapper extends BaseMapper<RequisitionOrder> {

    List<RequisitionOrderPageRecord> getPage(RequisitionOrderPageParam param);
}
