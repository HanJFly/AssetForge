package com.hjf.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonePageRecordVO {
    private Long id;
    private String processNo;
    private String businessType;
    private String title;
    private String decision;
    private LocalDateTime approvedAt;
}
