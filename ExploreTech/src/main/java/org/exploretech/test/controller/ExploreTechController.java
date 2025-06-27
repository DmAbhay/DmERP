package org.exploretech.test.controller;

import com.ctc.wstx.shaded.msv_core.driver.textui.Debug;
import com.fasterxml.jackson.databind.JsonNode;

import dataman.dmbase.redissessionutil.RedisObjectUtil;
import dataman.utility.test.util.Utils;

import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import org.exploretech.config.DynamicDataSourceBuilder;
import org.exploretech.config.ExternalConfigAuthService;
import org.exploretech.feign.AuthService;
import org.exploretech.feign.DatamanERPService;
import org.exploretech.feign.UtilityService;
import org.exploretech.util.UtilForSelf;
import org.exploretech.util.UtilForSelfG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@RestController
public class ExploreTechController {

    private final UtilityService devoteesClient;

    public ExploreTechController(UtilityService devoteesClient) {
        this.devoteesClient = devoteesClient;
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private DatamanERPService datamanERPService;

    @Autowired
    private UtilForSelf utilForSelf;

    @Autowired
    private UtilForSelfG utilForSelfG;



    @GetMapping("/devotee-info")
    public String getDevoteeInfo() {
        return devoteesClient.getDevoteeById();
    }

    @GetMapping("/ping")
    public String ping() {
        return "Temple Service is working!";
    }

    @GetMapping("/use-services")
    public String testYourServices(){
       return Utils.welcome();
    }

    @PostMapping("/register-student")
    public ResponseEntity<?> registerEmployee(@RequestBody JsonNode payload){
        return devoteesClient.registerUser(payload);
    }

    @PostMapping("/authenticate-token")
    public ResponseEntity<?> checkToken(@RequestHeader(value = "Authorization", required = true) String token){
        try{
            Map<String, String> result = authService.validateToken(token);
            System.out.println("Status : "+result.get("status"));
            System.out.println("Description : "+result.get("description"));
            return ResponseEntity.ok(result);

            //return ResponseEntity.ok(authService.validateToken(token));

        }catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "false");
            result.put("description", "Invalid token");
            return ResponseEntity.ok(result);

        }
    }


    @PostMapping("/authenticate-erp-token")
    public ResponseEntity<?> checkERPToken(@RequestHeader(value = "Authorization", required = true) String token){
        try{
            System.out.println("======================================================================================================================================================================================================================================================");
            System.out.println("token : "+token);
            System.out.println("======================================================================================================================================================================================================================================================");
            Map<String, String> result = datamanERPService.validateToken(token);
            System.out.println("Status : "+result.get("status"));
            System.out.println("Description : "+result.get("description"));
            return ResponseEntity.ok(result);

            //return ResponseEntity.ok(authService.validateToken(token));

        }catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "false");
            result.put("description", "Invalid token");
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/access-secure-api")
    public ResponseEntity<?> accessSecureApi(@RequestHeader(value = "Authorization", required = true) String token){

//        Map<String, String> result = utilForSelf.getResponse(token);
        Map<String, String> result = utilForSelfG.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        System.out.println(result);

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(Map.of("message", "Token is valid samjhe babu"));
    }

    @Autowired
    private ExternalConfigAuthService configService;

    @Autowired
    private DynamicDataSourceBuilder dynamicBuilder;

    private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";

    @Autowired
    private RedisObjectUtil redisObjectUtil;



    @GetMapping("/load-propertiesg")
    public ResponseEntity<?> loadProperties(){

        String configName = redisObjectUtil.getObjectValueAsString("configName", "configName");

        String host1 = configService.getProperty("myconfig1.properties", "sqlHostName");
        String host2 = configService.getProperty("myconfig2.properties", "sqlHostName");
        String host3 = configService.getProperty(configName, "sqlHostName");

        System.out.println(host1);
        System.out.println(host2);
        System.out.println(host3);


        File file = new File(CONFIG_DIR, configName);
        if (!file.exists()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Config file not found: " + file.getAbsolutePath()));
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);

            DataSource ds = dynamicBuilder.buildDataSource(props);
            JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

            return ResponseEntity.ok(Map.of(
                    "message", "Connected successfully",
                    "db", dbName,
                    "host", props.getProperty("sqlHostName")
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Connection failed: " + ex.getMessage()
            ));
        }
    }
}
