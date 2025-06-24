package org.exploretech.util;

import feign.FeignException;
import org.exploretech.feign.DatamanERPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UtilForSelf {

    @Autowired
    private DatamanERPService datamanERPService;

    public  Map<String, String> getResponse(String token) {

        try {
            return datamanERPService.validateTokenNew(token);
        } catch (FeignException.Forbidden ex) {
            // Handle 403 from Feign and return your custom response
            return Map.of(
                    "status", "false",
                    "description", "Invalid token"
            );
        } catch (FeignException ex) {
            return Map.of(
                    "status", "false",
                    "description", "Remote service error: " + ex.status()
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "false",
                    "description", "Unexpected error occurred"
            );
        }

    }
}
