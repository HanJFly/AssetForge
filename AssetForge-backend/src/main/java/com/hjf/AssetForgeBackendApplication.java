package com.hjf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;


@ServletComponentScan
@SpringBootApplication
@MapperScan("com.hjf.mapper")
public class AssetForgeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetForgeBackendApplication.class, args);
    }
    


}
