package com.keyboard.mes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 键盘装配 MES 后端启动类。
 *
 * <p>启动 Spring Boot 应用，并扫描 repository 包下的 MyBatis Mapper 接口。</p>
 *
 * @author Keyboard MES项目组
 */
@MapperScan("com.keyboard.mes.repository")
@SpringBootApplication
public class KeyboardMesApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeyboardMesApplication.class, args);
    }
}
