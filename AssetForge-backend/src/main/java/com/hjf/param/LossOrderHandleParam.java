package com.hjf.param;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LossOrderHandleParam {
    private Long id;
    private String handleType;
    private BigDecimal handleAmount;
    private String handlingRemark;
}
