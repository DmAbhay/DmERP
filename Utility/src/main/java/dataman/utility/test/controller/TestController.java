package dataman.utility.test.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @GetMapping("/get-test")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("jai shree krishna...!!!");
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> registerEmployee(@RequestBody JsonNode payload){
        return ResponseEntity.ok(payload);
    }


}
