package com.hjf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hjf.common.result.Result;
import com.hjf.entity.RequisitionOrder;
import com.hjf.entity.TransferOrder;
import com.hjf.param.TransferOrderDetailParam;
import com.hjf.param.TransferOrderPageParam;
import com.hjf.param.TransferOrderParam;
import com.hjf.service.ITransferOrderItemService;
import com.hjf.service.ITransferOrderService;
import com.hjf.vo.TransferOrderDetailVO;
import com.hjf.vo.TransferOrderPageVO;
import com.hjf.vo.TransferOrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 转移单主表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/transfer-order")
public class TransferOrderController {
    @Autowired
    private ITransferOrderService transferOrderService;


    /*
    * 创建转移单
    * */
    @PostMapping("/create")
    public Result<TransferOrderVO> create(@RequestBody @Valid TransferOrderParam param){
       TransferOrderVO vo =  transferOrderService.create(param);
       return Result.ok(vo);
    }


    /*
    * 转移单分页
    * */
    @PostMapping("/page")
    public Result<TransferOrderPageVO> page(@RequestBody TransferOrderPageParam param){
        TransferOrderPageVO vo = transferOrderService.querypage(param);
        return Result.ok(vo);
    }

    /*
    * 转移单详情
    * */
    @PostMapping("/detail")
    public Result<TransferOrderDetailVO> detail(@RequestBody TransferOrderDetailParam param){
        TransferOrderDetailVO vo = transferOrderService.detail(param);
        return Result.ok(vo);

    }

}
