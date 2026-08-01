package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.TransferOrder;
import com.hjf.param.TransferOrderPageParam;
import com.hjf.param.TransferOrderParam;
import com.hjf.vo.TransferOrderPageRecord;

import java.util.List;

/**
 * <p>
 * 转移单主表 Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface TransferOrderMapper extends BaseMapper<TransferOrder> {

    int queryTransferByParam(TransferOrderParam param);

    List<TransferOrderPageRecord> querypage(TransferOrderPageParam param);
}
