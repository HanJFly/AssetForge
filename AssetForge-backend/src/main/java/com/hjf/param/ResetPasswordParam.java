package com.hjf.param;

import lombok.Data;

@Data
public class ResetPasswordParam {
    private Long id;
    private String newPassword;
}
