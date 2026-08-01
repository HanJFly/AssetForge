package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class ApprovalRecordTodoPageParam extends Page {


    private String approvalType;

    private String targetType;
    private Long targetId;
    private Long approverId;
    private String approvalStatus;
}
