package com.approval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统启动类
 */
@SpringBootApplication
@MapperScan("com.approval.mapper")
public class ApprovalSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApprovalSystemApplication.class, args);
    }

}
