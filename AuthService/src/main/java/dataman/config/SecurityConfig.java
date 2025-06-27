package dataman.config;


import dataman.config.MD5PasswordEncoder;
import dataman.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        List<String> patternsList = new ArrayList<>();
        for (String config : listOfAllConfigs(System.getProperty("CONFIG_DIR"))) {
            patternsList.add("/api/datamanerp/" + config + "/centralized-login-new");
        }

        patternsList.add("/api/datamanerp/validate-token-new");
        patternsList.add("/api/datamanerp/centralized-login-new");
        patternsList.add("/api/datamanerp/employees/filter-records");
        patternsList.add("/api/datamanerp/employees/filter-recordss");
        patternsList.add("/swagger-ui/**");
        patternsList.add("/v3/api-docs/**");


        String[] patterns = patternsList.toArray(new String[0]);

        System.out.println("these are mentioned url Patterns " + Arrays.toString(patterns));
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(patterns).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MD5PasswordEncoder();  // Using MD5 (Not Recommended)
    }

    public ArrayList<String> listOfAllConfigs(String folderPath){


        ArrayList<String> res = new ArrayList<>();

        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) { // If you only want files (not directories)
                    //System.out.println("File: " + file.getName());

                    String fileName = file.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        fileName = fileName.substring(0, dotIndex);
                        System.out.println(fileName);
                        res.add(fileName);
                    }


                }
            }
        } else {
            System.out.println("Folder does not exist or it's not a directory.");
        }
        return res;

    }
}