package com.hjf.controller;

import com.hjf.common.result.Result;
import com.hjf.param.DepreciationRunLogExecuteParam;
import com.hjf.param.DepreciationRunLogPageParam;
import com.hjf.service.IDepreciationRunLogService;
import com.hjf.vo.DepreciationRunLogExecuteVO;
import com.hjf.vo.DepreciationRunLogPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 月度折旧执行记录表 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/depreciationRunLog")
public class DepreciationRunLogController {
    @Autowired
    private IDepreciationRunLogService depreciationRunLogService;

    /*
    * 手动执行折旧
    * */

    @PostMapping("/execute")
    public Result<DepreciationRunLogExecuteVO> execute(@RequestBody DepreciationRunLogExecuteParam param) {
        DepreciationRunLogExecuteVO vo = depreciationRunLogService.execute(param);
        return Result.ok(vo);
    }
    /*
    * 折旧执行记录分页
    * */
    @PostMapping("/page")
    public Result<DepreciationRunLogPageVO> page(@RequestBody DepreciationRunLogPageParam param) {
        DepreciationRunLogPageVO vo = depreciationRunLogService.qurypage(param);
        return Result.ok(vo);
    }

}
