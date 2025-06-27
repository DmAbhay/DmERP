package org.exploretech;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"org.exploretech", "dataman.dmbase.redissessionutil", "dataman.dmbase.customconfig", "dataman.dmbase.encryptiondecryptionutil", "dataman.dmbase.microservice"})
public class ExploreTech {
    public static void main(String[] args) {
        SpringApplication.run(ExploreTech.class, args);
        System.out.println("Hello, World!");
    }
}