package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.LossOrderDetailParam;
import com.hjf.param.LossOrderHandleParam;
import com.hjf.param.LossOrderPageParam;
import com.hjf.service.ILossOrderService;
import com.hjf.vo.LossOrderDetailVO;
import com.hjf.vo.LossOrderHandleVO;
import com.hjf.vo.LossOrderPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 盘亏单表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/lossOrder")
public class LossOrderController {
    @Autowired
    private ILossOrderService lossOrderService;

    /*
    * 盘亏单分页查询
    * */
    @PostMapping("/page")
    public Result<LossOrderPageVO> queryPage(@RequestBody LossOrderPageParam  param){
        LossOrderPageVO vo = lossOrderService.queryPage(param);
        return Result.ok(vo);
    }
    /*
    * 盘亏单详情
    * */
    @PostMapping("/detail")
    public Result<LossOrderDetailVO> detail(@RequestBody LossOrderDetailParam param){
        LossOrderDetailVO vo = lossOrderService.detail(param);
        return Result.ok(vo);
    }

    /*
    * 盘亏处理
    * */
    @PostMapping("/handle")
    public Result<LossOrderHandleVO> handle(@RequestBody LossOrderHandleParam param){
        LossOrderHandleVO vo = lossOrderService.handle(param);
        return Result.ok(vo);
    }


}
