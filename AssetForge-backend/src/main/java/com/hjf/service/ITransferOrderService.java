package com.hjf.service;

import com.hjf.entity.TransferOrder;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.TransferOrderDetailParam;
import com.hjf.param.TransferOrderPageParam;
import com.hjf.param.TransferOrderParam;
import com.hjf.vo.TransferOrderDetailVO;
import com.hjf.vo.TransferOrderPageVO;
import com.hjf.vo.TransferOrderVO;

/**
 * <p>
 * 转移单主表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ITransferOrderService extends IService<TransferOrder> {

    TransferOrderVO create(TransferOrderParam param);

    TransferOrderPageVO querypage(TransferOrderPageParam param);

    TransferOrderDetailVO detail(TransferOrderDetailParam param);
}
