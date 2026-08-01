package com.hjf.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserPageVO {
    private List<UserVO> records = new ArrayList<>();
    private long total;
    private int page;
    private int size;
}
