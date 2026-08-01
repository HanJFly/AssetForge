package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.entity.ReturnOrder;
import com.hjf.param.ReturnOrderConfirmInboundParam;
import com.hjf.param.ReturnOrderCreateParam;
import com.hjf.param.ReturnOrderDetailParam;
import com.hjf.param.ReturnOrderPageParam;
import com.hjf.service.IReturnOrderService;
import com.hjf.vo.ReturnOrderConfirmInboundVO;
import com.hjf.vo.ReturnOrderCreateVO;
import com.hjf.vo.ReturnOrderDetailVO;
import com.hjf.vo.ReturnOrderPageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 归还单主表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/return-order")
public class ReturnOrderController {

    @Autowired
    private IReturnOrderService returnOrderService;
    /*
    * 创建归还单
    * */
    @PostMapping("/create")
    public Result<ReturnOrderCreateVO> create(@RequestBody @Valid ReturnOrderCreateParam param){
        ReturnOrderCreateVO vo = returnOrderService.create(param);
        return Result.ok(vo);
    }

    /*
    * 归还单分页
    * */
    @PostMapping("/page")
    public Result<ReturnOrderPageVO> page(@RequestBody ReturnOrderPageParam param){
        ReturnOrderPageVO vo = returnOrderService.queryPage(param);
        return Result.ok(vo);
    }
    /*
    * 归还单详情
    * */
    @PostMapping("/detail")
    public Result<ReturnOrderDetailVO> detail(@RequestBody ReturnOrderDetailParam param){
        ReturnOrderDetailVO vo = returnOrderService.datail(param);
        return Result.ok(vo);
    }
    /*
    * 入库确认
    * */
    @PostMapping("/confirm-inbound")
    public Result<ReturnOrderConfirmInboundVO> inbound(@RequestBody ReturnOrderConfirmInboundParam param){
        ReturnOrderConfirmInboundVO vo = returnOrderService.inbound(param);
        return Result.ok(vo);
    }
}
