package com.hjf.param;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetCreateWithFilesParam {
    private String name;
    private Long categoryId;
    private Long departmentId;
    private String location;
    private String brandModel;
    private String specification;
    private String sourceType;
    private String purpose;
    private BigDecimal purchaseAmount;
    private LocalDate purchaseDate;
    private String supplier;
    private String remark;
}