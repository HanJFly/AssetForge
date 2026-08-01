package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.InventoryDetailPageParam;
import com.hjf.param.InventoryDetailSubmitParam;
import com.hjf.service.IInventoryDetailService;
import com.hjf.vo.InventoryDetailPageVO;
import com.hjf.vo.InventoryDetailSubmitVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 盘点明细表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/inventoryDetail")
public class InventoryDetailController {
    @Autowired
    private IInventoryDetailService inventoryDetailService;
    /*
    * 查询盘点明细
    * */
    @PostMapping("/page")
    public Result<InventoryDetailPageVO> queryPage(@RequestBody InventoryDetailPageParam param){
        InventoryDetailPageVO vo = inventoryDetailService.queryPage(param);
        return Result.ok(vo);
    }

    /*
    * 提交盘点
    * */
    @PostMapping("/submit")
    public Result<InventoryDetailSubmitVO> submit(@RequestBody @Valid InventoryDetailSubmitParam param){
        InventoryDetailSubmitVO vo = inventoryDetailService.submit(param);
        return Result.ok(vo);
    }

}
