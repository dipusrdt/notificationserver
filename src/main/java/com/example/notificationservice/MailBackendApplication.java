package com.example.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MailBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailBackendApplication.class, args);
        System.out.print("let's do it");}

}
