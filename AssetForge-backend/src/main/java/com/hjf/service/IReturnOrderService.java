package com.hjf.service;

import com.hjf.entity.ReturnOrder;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.ReturnOrderConfirmInboundParam;
import com.hjf.param.ReturnOrderCreateParam;
import com.hjf.param.ReturnOrderDetailParam;
import com.hjf.param.ReturnOrderPageParam;
import com.hjf.vo.ReturnOrderConfirmInboundVO;
import com.hjf.vo.ReturnOrderCreateVO;
import com.hjf.vo.ReturnOrderDetailVO;
import com.hjf.vo.ReturnOrderPageVO;
import jakarta.validation.Valid;

/**
 * <p>
 * 归还单主表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IReturnOrderService extends IService<ReturnOrder> {

    ReturnOrderCreateVO create(@Valid ReturnOrderCreateParam param);

    ReturnOrderPageVO queryPage(ReturnOrderPageParam param);

    ReturnOrderDetailVO datail(ReturnOrderDetailParam param);

    ReturnOrderConfirmInboundVO inbound(ReturnOrderConfirmInboundParam param);
}
