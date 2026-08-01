package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.RequisitionOrderCreateParam;
import com.hjf.param.RequisitionOrderDetailParam;
import com.hjf.param.RequisitionOrderOutBoundParam;
import com.hjf.param.RequisitionOrderPageParam;
import com.hjf.service.IRequisitionOrderService;
import com.hjf.vo.RequisitionOrderCreateVO;
import com.hjf.vo.RequisitionOrderDetailVO;
import com.hjf.vo.RequisitionOrderOutBoundVO;
import com.hjf.vo.RequisitionOrderPageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 申领单主表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/receive-order")
public class RequisitionOrderController {

    @Autowired
    private IRequisitionOrderService requisitionOrderService;

    /*
    * 创建申领单
    * */
    @PostMapping("/create")
    public Result<RequisitionOrderCreateVO> create(@RequestBody @Valid RequisitionOrderCreateParam param) {
        RequisitionOrderCreateVO vo = requisitionOrderService.create(param);
        return Result.ok(vo);
    }

    /*
    * 申领单分页查询
    * */
    @PostMapping("/page")
    public Result<RequisitionOrderPageVO> page(@RequestBody RequisitionOrderPageParam param) {
        RequisitionOrderPageVO vo = requisitionOrderService.queryPage(param);
        return Result.ok(vo);
    }

    /*
    * 申领单详情
    * */
    @PostMapping("/detail")
    public Result<RequisitionOrderDetailVO> detail(@RequestBody RequisitionOrderDetailParam param) {
        return Result.ok(requisitionOrderService.detail(param));
    }

    /*
    * 出库确认
    * */
    @PostMapping("/confirm-outbound")
    public Result<RequisitionOrderOutBoundVO> comfirmOutbound(@RequestBody @Valid RequisitionOrderOutBoundParam param) {
        return Result.ok(requisitionOrderService.comfirmOutbound(param));
    }
}
