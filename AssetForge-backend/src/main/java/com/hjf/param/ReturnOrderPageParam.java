package com.hjf.param;

import cn.hutool.core.date.chinese.SolarTerms;
import com.hjf.vo.Page;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
public class ReturnOrderPageParam extends Page {
    private String approvalStatus;
    //模糊查询
    private String orderNo;
    private String returnUserName;
    private String returnUserEmployeeNo;
    private String returnUserDepartmentName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
