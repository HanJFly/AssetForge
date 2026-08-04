package com.hjf.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LossOrderDetailVO {
    private Long id;
    private String orderNo;
    private Long inventoryTaskId;
    private Long inventoryTaskDetailId;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long responsibleUserId;
    private String responsibleUserName;
    private Long responsibleDepartmentId;
    private String responsibleDepartmentName;
    private BigDecimal lossAmount;
    private String compensationStatus;
    private String remark;
    private BigDecimal suggestedCompensation;  // 建议赔偿金额
    private BigDecimal actualCompensation;
}
