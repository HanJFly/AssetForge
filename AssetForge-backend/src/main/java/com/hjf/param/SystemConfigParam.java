package com.hjf.param;

import com.hjf.entity.SystemConfig;
import lombok.Data;

import java.util.List;

@Data
public class SystemConfigParam extends SystemConfig {
    private List<SystemConfig> systemConfigs;
}
