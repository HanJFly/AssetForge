package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalRecordDetailVO {
    private Long id;
    private String processNo;
    private String businessType;
    private Long businessId;
    private String title;
    private String status;
    private Long applicantId;
    private String applicantName;
    private Long currentApproverId;
    private String currentApproverName;
    private List<ApprovalRecordDetailFormData> formData = new ArrayList<>();
    private List<ApprovalRecordDetailHistoryList> historyList = new ArrayList<>();
}
