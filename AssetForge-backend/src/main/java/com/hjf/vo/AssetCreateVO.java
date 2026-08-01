package com.hjf.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetCreateVO {
    @NotNull(message = "资产id不能为空")
    private Long id;
    @NotBlank(message = "资产编号不能为空")
    private String assetCode;


}
