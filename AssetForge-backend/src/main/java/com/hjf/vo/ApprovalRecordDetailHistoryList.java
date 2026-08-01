package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalRecordDetailHistoryList {
    private String approverName;
    private String decision;
    private String comment;
    private LocalDateTime actionTime;

}
