package com.hjf.service;

import com.hjf.entity.LossOrder;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.LossOrderDetailParam;
import com.hjf.param.LossOrderHandleParam;
import com.hjf.param.LossOrderPageParam;
import com.hjf.vo.LossOrderDetailVO;
import com.hjf.vo.LossOrderHandleVO;
import com.hjf.vo.LossOrderPageVO;

/**
 * <p>
 * 盘亏单表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ILossOrderService extends IService<LossOrder> {

    LossOrderPageVO queryPage(LossOrderPageParam param);

    LossOrderDetailVO detail(LossOrderDetailParam param);

    LossOrderHandleVO handle(LossOrderHandleParam param);
}
