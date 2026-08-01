package com.hjf.vo;

import lombok.Data;

import java.awt.print.PrinterAbortException;
import java.util.ArrayList;
import java.util.List;

@Data
public class RequisitionOrderDetailVO {
    private Long id;
    private String orderNo;
    private Long applicantId;
    private String applicantName;
    private String reason;
    private String approvalStatus;
    private List<RequisitionOrderDetailItemVO> itemList = new ArrayList<>();

}
