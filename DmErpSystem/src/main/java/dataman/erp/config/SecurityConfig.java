package dataman.erp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private FeignTokenValidationFilter feignTokenValidationFilter;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                .cors(Customizer.withDefaults())
//                .csrf(csrf -> csrf.disable())
//                .httpBasic(Customizer.withDefaults()) // Optional: disable this if not needed
//                .formLogin(form -> form.disable())    // 🚫 Disable default login form
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/api/datamanerp/login",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**"
//                        ).permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(feignTokenValidationFilter, UsernamePasswordAuthenticationFilter.class)
//                .build();
//    }
//}


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private FeignTokenValidationFilter feignTokenValidationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/datamanerp/login","/api/datamanerp/rest","/api/datamanerp/employees/filter","/api/datamanerp/centralized-login",
                                "/api/datamanerp/employees/filter-records", "/api/datamanerp/employees/filter-recordss", "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(feignTokenValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }




}
