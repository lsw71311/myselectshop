package com.sparta.myselectshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling   //메서드를 특정 간격 또는 시간에 실행되도록 스케줄링 활성화
@EnableJpaAuditing //엔티티 객체가 생성이 되거나 변경이 되었을 때 자동으로 값을 등록
@SpringBootApplication
public class MyselectshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyselectshopApplication.class, args);
    }

}
