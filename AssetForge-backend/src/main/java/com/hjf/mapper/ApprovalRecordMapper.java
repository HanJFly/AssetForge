package com.hjf.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hjf.entity.ApprovalRecord;
import com.hjf.param.ApprovalRecordDonePageParam;
import com.hjf.param.ApprovalRecordTodoPageParam;

import java.util.List;

/**
 * <p>
 * 审批记录表（统一审批流） Mapper 接口
 * </p>
 *
 * @author Baomidou
 * @since 2026-07-18
 */
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    List<ApprovalRecord> todoPage(ApprovalRecordTodoPageParam param);

    List<ApprovalRecord> donePage(ApprovalRecordDonePageParam param);
}
