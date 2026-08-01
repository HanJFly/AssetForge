package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApprovalRecordDetailFormData {
    private String orderNo;
    private List<itemList> itemList = new ArrayList<>();
    private String reason;
    @Data
    public static class itemList{
        private Long categoryId;
        private String categoryName;
        private int quantity;
    }
}
