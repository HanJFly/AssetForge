package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class ApprovalRecordDonePageParam extends Page {

    private String approvalType;


    private String targetType;


    private String approvalStatus;
}
