package org.cyducks.satark;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
@Slf4j
public class FirebaseServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FirebaseServiceApplication.class, args);
    }
}
