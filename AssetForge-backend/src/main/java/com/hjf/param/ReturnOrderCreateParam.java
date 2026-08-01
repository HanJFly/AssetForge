package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReturnOrderCreateParam {
    @NotBlank(message = "归还原因不能为空")
    private String reason;
    @NotNull(message = "归还资产明细单不能为空")
    private List<Item> itemList;
    @NotNull(message = "预计归还日期不能为空")
    private LocalDate expectedReturnDate;

    @Data
    public static class Item{
        @NotNull(message = "归还资产Id不能为空")
        private Long assetId;
        @NotBlank(message = "归还资产状况不能为空")
        private String assetCondition;
        @NotBlank(message = "归还时资产状况文字描述不能为空")
        private String conditionRemark;
    }

}
