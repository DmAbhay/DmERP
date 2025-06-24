package dataman.utility.feign;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "DatamanAuthService")
public interface DatamanERPService {

    @GetMapping("/api/datamanerp/validate-token")
    Map<String, String> validateToken(@RequestHeader(value = "Authorization", required = true) String token);

    @GetMapping("/api/datamanerp/validate-token-new")
    Map<String, String> validateTokenNew(@RequestHeader(value = "Authorization", required = true) String token);
}
