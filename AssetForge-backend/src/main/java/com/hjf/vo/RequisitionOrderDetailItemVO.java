package com.hjf.vo;

import lombok.Data;

@Data
public class RequisitionOrderDetailItemVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Integer quantity;
    private String assetCode;
    private String assetName;

}
