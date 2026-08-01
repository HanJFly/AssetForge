package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequisitionOrderPageParam extends Page {

    private String approvalStatus;
    private String orderNo;
    private String applicantName;
    private String departmentName;
    private String approverName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
