package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScrapOrderPageParam extends Page {
    private String approavalStatus;

    //模糊查询
    private String orderNo;
    private String applicantName;
    private String applicantEmployeeNo;
    private String applicantDepartmentName;
    private String approvalStatus;
    private String approvalName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
