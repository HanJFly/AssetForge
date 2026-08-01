package com.hjf.service;

import com.hjf.entity.RequisitionOrder;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.RequisitionOrderCreateParam;
import com.hjf.param.RequisitionOrderDetailParam;
import com.hjf.param.RequisitionOrderOutBoundParam;
import com.hjf.param.RequisitionOrderPageParam;
import com.hjf.vo.RequisitionOrderCreateVO;
import com.hjf.vo.RequisitionOrderDetailVO;
import com.hjf.vo.RequisitionOrderOutBoundVO;
import com.hjf.vo.RequisitionOrderPageVO;
import jakarta.validation.Valid;

/**
 * <p>
 * 申领单主表 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IRequisitionOrderService extends IService<RequisitionOrder> {

    RequisitionOrderCreateVO create(RequisitionOrderCreateParam param);

    RequisitionOrderPageVO queryPage(RequisitionOrderPageParam param);

    RequisitionOrderDetailVO detail(RequisitionOrderDetailParam param);

    RequisitionOrderOutBoundVO comfirmOutbound(@Valid RequisitionOrderOutBoundParam param);
}
