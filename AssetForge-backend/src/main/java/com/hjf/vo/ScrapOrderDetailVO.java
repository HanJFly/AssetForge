package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScrapOrderDetailVO {
    private Long id;
    private String orderNo;
    private String reason;
    private String approvalStatus;
    private List<ScrapOrderItemDetailVO> itemList = new ArrayList<>();
    private List<ScrapOrderDetailFileVO> attachmentList = new ArrayList<>();

    private String createTime;




}
