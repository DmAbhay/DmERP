package dataman.utility.userMast;

import dataman.dmbase.microservice.MsUtil;
import dataman.dmbase.paging.dto.SearchRequest;


import dataman.utility.feign.DatamanERPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/dataman/utility")
public class UserController {

    private final UserMastServices userMastService;


    @Autowired
    private MsUtil msUtil;

    @Autowired
    private DatamanERPService datamanERPService;

    @Autowired
    public UserController(UserMastServices userMastService) {
        this.userMastService = userMastService;
    }

    @Autowired
    @Qualifier("companyNamedJdbcTemplate")
    private NamedParameterJdbcTemplate npjt;

    @GetMapping("/get-user")
    public ResponseEntity<Map<String, Object>> getUserDetails(@RequestParam("userName") String userName) {
        try {
            // Calling the service method to fetch user details
            Map<String, Object> userDetails = userMastService.getUserDetailsWithLinkedAccounts(userName);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            // Handle case where user details are not found
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
    }


    @DeleteMapping("/delete")
    public String deleteUserByCode(@RequestParam String userName) {
        return userMastService.deleteUserByCode(userName);
    }


    @GetMapping("/find")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "") String prefix,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Map<String, Object>> result = userMastService.getUsers(prefix, page, size);

        boolean isLast = result.size() < size;

        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("isLast", isLast);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/get-users")
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        String uName = result.get("username");

        String redisKey = uName + "_" + token.substring(7);

        //String redisKey = util.getRedisKey(token);
        return ResponseEntity.ok(userMastService.getFind(searchRequest, npjt, redisKey));

    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String username) throws Exception {

        userMastService.deleteUser(username, npjt);
        return ResponseEntity.ok("User Deleted successfully");

    }

}
