package com.hjf.vo;

import lombok.Data;

@Data
public class ReturnOrderDetaiItemVO {
    private Long assetID;
    private String assetCode;
    private String assetName;
    private String returnCondition;
    private String conditionRemark;
}
