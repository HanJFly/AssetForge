package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransferOrderDetailVO {
    private Long id;
    private String orderNo;
    private Long fromDepartmentId;
    private String fromDepartmentName;
    private Long toDepartmentId;
    private String toDepartmentName;
    private Long fromUserId;
    private String fromUserName;
    private Long toUserId;
    private String toUserName;
    private String reason;
    private String approvalStatus;

    private List<TransferOrderDetailItemVO> itemList = new ArrayList<>();

}
