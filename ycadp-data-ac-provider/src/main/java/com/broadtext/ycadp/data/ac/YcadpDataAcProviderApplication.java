package com.broadtext.ycadp.data.ac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * 启动类
 *
 * @author xuchenglong
 */
@EnableCircuitBreaker
@EnableFeignClients
@EnableHystrix
@SpringBootApplication
@EnableJpaAuditing
@EnableKafka
public class YcadpDataAcProviderApplication {

	/**
	 *
	 * 启动方法
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(YcadpDataAcProviderApplication.class, args);
	}

}
