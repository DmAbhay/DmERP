package org.exploretech.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;


@FeignClient(name = "auth-service") // must match DevoteesService spring.application.name
public interface AuthService {

    @GetMapping("/validate-token")
    Map<String, String> validateToken(@RequestHeader(value = "Authorization", required = true) String token);

    @PostMapping("/register-user")
    ResponseEntity<?> registerUser(@RequestBody JsonNode payload);
}




