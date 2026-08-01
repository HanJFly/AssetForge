package com.hjf.param;

import lombok.Data;

@Data
public class ReturnOrderConfirmInboundParam {
    private Long id;
    private String storageLocation;
    private String confirmRemark;
}
