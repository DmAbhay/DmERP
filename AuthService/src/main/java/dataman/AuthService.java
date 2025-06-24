package dataman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {
                    "dataman.config",  // or "dataman.utility.config", whichever is correct
                    "dataman.auth",
                    "dataman.jwt",
                    "dataman.dmbase.redissessionutil",
                    "dataman.dmbase.customconfig",
                    "dataman.dmbase.encryptiondecryptionutil"}
              )

public class AuthService {
    public static void main(String[] args) {
        SpringApplication.run(AuthService.class, args);
        System.out.println("Hello, World!");
    }
}