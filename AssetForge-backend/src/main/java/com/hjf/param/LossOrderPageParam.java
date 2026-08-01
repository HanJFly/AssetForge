package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LossOrderPageParam extends Page {
    private String compensationStatus;
    private String orderNo;
    private String  assetCode;
    private  String assetName;
    private String categoryName;
    private String responsibleUserName;
    private String responsibleUserEmployeeNo;
    private String responsibleDepartmentName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;





}
