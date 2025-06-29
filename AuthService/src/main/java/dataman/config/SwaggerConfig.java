package dataman.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Dataman ERP API",
                version = "1.0",
                description = "API Documentation for Dataman ERP",
                contact = @Contact(name = "Support Team", email = "abhaykumar.pandey@dataman.in")
        )
)
public class SwaggerConfig {
}
