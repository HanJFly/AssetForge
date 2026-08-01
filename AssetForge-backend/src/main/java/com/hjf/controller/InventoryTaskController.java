package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.*;
import com.hjf.service.IInventoryDetailService;
import com.hjf.service.IInventoryTaskService;
import com.hjf.vo.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 盘点任务表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/inventoryTask")
public class InventoryTaskController {

    @Autowired
    private IInventoryTaskService inventoryTaskService;


    /*
    * 创建盘点任务
    * */
    @PostMapping("/create")
    public Result<InventoryTaskCreateVO> create(@RequestBody @Valid InventoryTaskCreateParam param){
        InventoryTaskCreateVO vo = inventoryTaskService.create(param);
        return Result.ok(vo);
    }
    /*
    * 盘点任务分页
    * */
    @PostMapping("/page")
    public Result<InventoryTaskPageVO> queryPage(@RequestBody InventoryTaskPageParam param){
        InventoryTaskPageVO vo = inventoryTaskService.queryPage(param);
        return Result.ok(vo);
    }

    /*
    * 盘点任务详情
    * */
    @PostMapping("/detail")
    public Result<InventoryTaskDeatilVO> queryDetail(@RequestBody InventoryTaskDeatilParam param){
        InventoryTaskDeatilVO vo = inventoryTaskService.detail(param);
        return Result.ok(vo);
    }


    /*
    * 盘点报告
    * */
    @PostMapping("/report")
    public Result<InventoryTaskReportVO> report(@RequestBody InventoryTaskReportParam param){
        InventoryTaskReportVO vo = inventoryTaskService.report(param);
        return Result.ok(vo);
    }
    /*
    * 更新盘点结论
    * */
    @PostMapping("/conclusion")
    public Result<InventoryTaskConclusionVO> updateConclusion(@RequestBody InventoryTaskConclusionParam param){
        InventoryTaskConclusionVO vo = inventoryTaskService.updateConclusion(param);
        return Result.ok(vo);



    }





}
