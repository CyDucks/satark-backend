package org.cyducks.satark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "org.cyducks")
@EntityScan("org.cyducks")
@EnableJpaRepositories("org.cyducks")
public class SatarkApplication {
    public static void main(String[] args) {
        SpringApplication.run(SatarkApplication.class, args);
    }
}
