package com.hjf.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequisitionOrderOutBoundParam {
    @NotNull(message = "id不能为空")
    private Long id;
    @NotNull(message = "出库意见不能为空")
    private String confirmRemark;
    private List<Item> itemList;



    @Data
    public static class Item{
        private Long itemId;
        private Long assetId;
    }



}
