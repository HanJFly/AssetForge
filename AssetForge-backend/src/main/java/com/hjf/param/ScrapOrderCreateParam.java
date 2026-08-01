package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScrapOrderCreateParam {
    @NotBlank(message = "报废原因不能为空")
    private String reason;
    @NotNull(message = "报废单资产明细不能为空")
    private List<Item> itemList;
    @NotNull(message = "附件不能为空")
    private List<Long> attachmentIds = new ArrayList<>();


    @Data
    public static class Item{
        private Long assetId;
    }
}
