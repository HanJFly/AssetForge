package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.SystemConfigParam;
import com.hjf.service.ISystemConfigService;
import com.hjf.vo.SystemConfigVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 系统配置表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/system/config")
public class SystemConfigController {

    @Autowired
    private ISystemConfigService systemConfigService;

    //获取系统配置详情
    @PostMapping("/detail")
    public Result<List<SystemConfigVo>> detail() {
        List<SystemConfigVo> list =systemConfigService.detail();
        return Result.ok(list);
    }

    /*
    * 批量保存系统配置
    * */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody List<SystemConfigParam> param) {
        systemConfigService.update(param);
        return Result.ok();
    }
}
