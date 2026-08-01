package com.hjf.param;

import lombok.Data;

@Data
public class ApprovalRecordTransferParam {
    private Long id;
    private Long targetApproverId;
    private String comment;
}
