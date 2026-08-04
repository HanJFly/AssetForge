package com.hjf.service;

import com.github.pagehelper.Page;
import com.hjf.entity.ApprovalRecord;
import com.baomidou.mybatisplus.spring.service.IService;
import com.hjf.param.*;
import com.hjf.vo.*;
import java.util.List;

/**
 * <p>
 * 审批记录表（统一审批流） 服务类
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface IApprovalRecordService extends IService<ApprovalRecord> {

    ApprovalRecordTodoPageVO todoPage(ApprovalRecordTodoPageParam param);

    ApprovalRecordDonePageVO donePage(ApprovalRecordDonePageParam param);

    ApprovalRecordDetailVO detail(ApprovalRecordDetailParam  param);

    ApprovalRecordActionVO action(ApprovalRecordActionParam param);

    ApprovalRecordTransferVO transfer(ApprovalRecordTransferParam param);

    List<ApprovalApproverVO> approverList();
}
