package com.broadtext.ycadp.data.ac.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YcadpDataAcProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(YcadpDataAcProviderApplication.class, args);
	}

}
