package org.exploretech.util;

import feign.FeignException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Callable;

@Service
public class UtilForSelfG {

    public <T> Map<String, String> getGenericResponse(Callable<T> feignCall) {
        try {
            T result = feignCall.call();

            if (result instanceof Map<?, ?> mapResult) {
                //noinspection unchecked
                return (Map<String, String>) mapResult;
            }

            return Map.of(
                    "status", "true",
                    "description", "Success",
                    "data", result.toString()
            );

        } catch (FeignException.Forbidden ex) {
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
