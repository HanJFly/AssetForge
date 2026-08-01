package com.hjf.controller;

import com.github.pagehelper.Page;
import com.hjf.common.result.Result;
import com.hjf.param.*;
import com.hjf.service.IApprovalRecordService;
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
 * 审批记录表（统一审批流） 前端控制器
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
@RestController
@RequestMapping("/approval")
public class ApprovalRecordController {
    @Autowired
    private IApprovalRecordService approvalRecordService;

    /*
    * 待审批分页
    * */
    @PostMapping("/todo/page")
    public Result<ApprovalRecordTodoPageVO> todoPage(@RequestBody ApprovalRecordTodoPageParam param) {
         ApprovalRecordTodoPageVO vo= approvalRecordService.todoPage(param);
       return Result.ok(vo);
    }

    /*
    * 已审批分页
    * */
    @PostMapping("/done/page")
    public Result<ApprovalRecordDonePageVO> donePage(@RequestBody ApprovalRecordDonePageParam param){
        ApprovalRecordDonePageVO vo= approvalRecordService.donePage(param);
        return Result.ok(vo);
    }

    /*
    * 审批详情
    * */
    @PostMapping("/detail")
    public Result<ApprovalRecordDetailVO> detail(@RequestBody ApprovalRecordDetailParam param){
        ApprovalRecordDetailVO vo= approvalRecordService.detail(param);
        return Result.ok(vo);
    }

    /*
    * 审批操作
    * */
    @PostMapping("/action")
    public Result<ApprovalRecordActionVO> action(@RequestBody @Valid ApprovalRecordActionParam param){
        ApprovalRecordActionVO vo= approvalRecordService.action(param);
        return Result.ok(vo);
    }

    /*
    * 转交审批
    * */
    @PostMapping("/transfer")
    public Result<ApprovalRecordTransferVO> transfer(@RequestBody ApprovalRecordTransferParam param){
        ApprovalRecordTransferVO vo= approvalRecordService.transfer(param);
        return Result.ok(vo);
    }

}
