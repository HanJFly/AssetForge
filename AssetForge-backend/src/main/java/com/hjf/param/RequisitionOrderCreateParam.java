package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RequisitionOrderCreateParam {
    @NotBlank(message = "申请原因不能为空")
    private String reason;

    private LocalDate expectedDate;
    @NotEmpty(message = "物品列表不能为空")
    private List<itemList> itemList;

    @Data
    public static class itemList {
        private Long categoryId;
        private int quantity;
    }
}
