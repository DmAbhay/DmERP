package dataman.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = {"dataman.erp", "dataman.dmbase.redissessionutil", "dataman.dmbase.customconfig", "dataman.dmbase.encryptiondecryptionutil", "dataman.dmbase.microservice"})
public class DmErpsys {
    public static void main(String[] args) {
        SpringApplication.run(DmErpsys.class, args);
        System.out.println("Hello, World!");
    }
}