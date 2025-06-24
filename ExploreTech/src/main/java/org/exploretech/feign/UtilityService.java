package org.exploretech.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "utility") // must match DevoteesService spring.application.name
public interface UtilityService {

    @GetMapping("/get-test")
    String getDevoteeById();

    @PostMapping("/register-user")
    ResponseEntity<?> registerUser(@RequestBody JsonNode payload);
}