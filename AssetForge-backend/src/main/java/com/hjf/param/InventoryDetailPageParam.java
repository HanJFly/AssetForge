package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class InventoryDetailPageParam extends Page {
    private Long taskId;
    private String result;

}
