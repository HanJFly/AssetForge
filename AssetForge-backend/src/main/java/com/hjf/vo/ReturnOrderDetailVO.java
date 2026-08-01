package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReturnOrderDetailVO {
    private Long id;
    private String orderNo;
    private String reason;
    private String approvalStatus;

    private List<ReturnOrderDetaiItemVO> itemList = new ArrayList<>();


}
