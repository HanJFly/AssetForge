package com.hjf.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TransferOrderParam {
    @NotNull(message = "原部门Id不能为空")
    private Long fromDepartmentId;
    @NotNull(message = "新部门Id不能为空")
    private Long toDepartmentId;
    @NotNull(message = "原用户不能为空")
    private Long fromUserId;
    @NotNull(message = "新用户不能为空")
    private Long toUserId;
    @NotBlank(message = "原因不能为空")
    private String reason;

    @NotNull(message = "转移资产明细不能为空")
    private List<Item> itemList;

    @Data
    public static class Item{

        private Long assetId;

    }


}
