package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.entity.ScrapOrder;
import com.hjf.param.ScrapOrderCreateParam;
import com.hjf.param.ScrapOrderDetailParam;
import com.hjf.param.ScrapOrderPageParam;
import com.hjf.service.IScrapOrderItemService;
import com.hjf.vo.ScrapOrderCreateVO;
import com.hjf.vo.ScrapOrderDetailVO;
import com.hjf.vo.ScrapOrderPageRecord;
import com.hjf.vo.ScrapOrderPageVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 报废单主表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/scrapOrder")
public class ScrapOrderController {
    @Autowired
    private IScrapOrderItemService scrapOrderItemService;

    /*
    * 创建报废单
    * */
    @PostMapping("/create")
    public Result<ScrapOrderCreateVO> create(@RequestBody @Valid ScrapOrderCreateParam param){
        ScrapOrderCreateVO vo =  scrapOrderItemService.create(param);
        return Result.ok(vo);
    }

    /*
    * 分页查询
    * */
    @PostMapping("/page")
    public Result<ScrapOrderPageVO> queryPage(@RequestBody ScrapOrderPageParam param){
        ScrapOrderPageVO vo = scrapOrderItemService.queryPage(param);
        return Result.ok(vo);
    }

    /*
    * 报废单详情
    * */
    @PostMapping("/detail")
    public Result<ScrapOrderDetailVO> detail(@RequestBody ScrapOrderDetailParam param){
        ScrapOrderDetailVO vo = scrapOrderItemService.detail(param);
        return Result.ok(vo);
    }
}
