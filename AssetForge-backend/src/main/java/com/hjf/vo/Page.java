package com.hjf.vo;

import com.hjf.entity.User;
import lombok.Data;

@Data
public class Page<T> {
    int page = 1; // 当前页码
    int size = 10; // 每页显示的记录数
    int total; // 总记录数

}
