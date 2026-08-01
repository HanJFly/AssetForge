package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransferOrderPageParam extends Page {
    private String orderNo;
    private String fromUserName;
    private String fromUserDepartmentName;
    private String toUserName;
    private String toUserDepartmentName;
    private String approverName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String approvalStatus;

}
