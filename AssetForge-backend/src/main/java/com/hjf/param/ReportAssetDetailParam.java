package com.hjf.param;

import com.hjf.vo.Page;
import lombok.Data;

@Data
public class ReportAssetDetailParam extends Page {
    private Long categoryId;
    private Long departmentId;
    private String status;

}
