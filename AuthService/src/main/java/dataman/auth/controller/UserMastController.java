package dataman.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import dataman.auth.dto.AuthRequestDTO;
import dataman.auth.dto.LoginResponseDTO;

import dataman.auth.dto.UserMast;
import dataman.auth.repository.UserMastRepository;
import dataman.auth.service.UserMastService;
import dataman.auth.util.Util;
import dataman.config.DynamicDataSourceBuilder;
//import dataman.config.ExternalConfigAuthService;
import dataman.jwt.JwtTokenUtil;
import dataman.dmbase.debug.Debug;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import dataman.dmbase.redissessionutil.AuthKeyUtil;
import dataman.dmbase.redissessionutil.RedisObjectUtil;
import dataman.dmbase.redissessionutil.RedisSimpleKeyValuePairUtil;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.dmbase.utils.DmUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/datamanerp")
public class UserMastController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMastService userMastService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisSimpleKeyValuePairUtil redisSimpleKeyValuePairUtil;

//    @Autowired
//    private ExternalConfigAuthService externalConfig;



    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AuthKeyUtil authKeyUtil;

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Autowired
    private UserMastRepository userMastRepository;

    @Autowired
    private Util util;



    private String generateMD5Hash(String input) {


        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02X", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 Algorithm not found", e);
        }
    }




//    @PostMapping("/centralized-login")
//    public ResponseEntity<?> centralizedLogin(@RequestBody AuthRequestDTO authRequest) {
//
//        try {
//
//            String loginMode = DmUtil.getLoginMode(authRequest.getUserName());
//            System.out.println(loginMode);
//            System.out.println(authRequest);
//            //System.out.println(LoginMode.getByCode(1));
//
//            if(!loginMode.equals("USERNAME")){
//                //String loginMode = LoginMode.getByCode(Integer.parseInt(authRequest.getLoginMode()));
//
//
//                System.out.println(loginMode);
//                Map<String, String> userCredentials = util.getUserCredentials(authRequest.getUserName(), loginMode);
//
//                if(userCredentials != null){
//
//                    authRequest.setUserName(userCredentials.get("user_Name"));
//                    //authRequest.setPassword(userCredentials.get("passWd"));
//
//                }else{
//                    throw new BadCredentialsException("Invalid email or password");
//                }
//            }
//
//            System.out.println(authRequest);
//
//            // Authenticate the user
//            System.out.println(authRequest.getUserName());
//            System.out.println(authRequest.getPassword());
//
////            if(Integer.parseInt(authRequest.getLoginMode()) == 3){
////                System.out.println("Come as for execution");
////                authenticationManager.authenticate(
////                        new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
////                );
////            }
//
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getUserName().toLowerCase()+authRequest.getPassword())
//            );
//
//
//        } catch (BadCredentialsException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//
//        System.out.println("User Validated!!!!!!!!!!!!!!!!!!");
//
//
//        // Fetch user details
//        UserDetails userDetails = userMastService.loadUserByUsername(authRequest.getUserName());
//
//        // Generate JWT token
//        String token = jwtTokenUtil.generateToken(userDetails.getUsername());
//
//        // Optionally, store the token in a cache (e.g., Redis) with the associated username for further validation
//
//        HashMap<String, String> hm = new HashMap<>();
//
//        hm.put("token", token);
//        String companyDB = externalConfig.getCompanyDb();
//
////        hm.put("companyDB", companyDB);
////        hm.put("username", authRequest.getUserName());
//
//
//        System.out.println(hm);
//        String key = authRequest.getUserName().toLowerCase() + "_"+ token;    //<AP> TBR : I have to rename key to redisKey
//        redisObjectUtil.saveObject(key, hm, 60, TimeUnit.MINUTES);
//        redisObjectUtil.addFieldToObject(key, "password", generateMD5Hash(authRequest.getUserName().toLowerCase()+authRequest.getPassword()));
//
////        redisObjectUtil.addFieldToObject(key, "address", "Buxar, Bihar");
////        redisObjectUtil.addFieldToObject(key, "username", authRequest.getUserName());
////        redisObjectUtil.addFieldToObject(key, "loginDate", authRequest.getLoginDate());
//
//        String loginDate = DmUtil.convertUnixTimestampToFormattedDate(authRequest.getLoginDate());
//        redisUtil.setRedisUserName(key, authRequest.getUserName());
//        redisUtil.setRedisLoginDate(key, loginDate);
//        redisUtil.setRedisDBName(key, companyDB);
//
//
//
//        String authKey = authKeyUtil.generateAuthKey();
//        authKeyUtil.storeAuthKey(authKey, 60*60*1000);
//
//        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
//        loginResponseDTO.setAuthKey(encryptionDecryptionUtil.encrypt(authKey));
//        loginResponseDTO.setToken(token);
//        loginResponseDTO.setSecretKey(null);
//
//        return ResponseEntity.ok(loginResponseDTO);
//
//    }





    @GetMapping("/test-service")
    public ResponseEntity<?> test(){
        return ResponseEntity.ok("jai shree krishna");
    }

//    @GetMapping("/validate-token")
//    public Map<String, String> validateToken(@RequestHeader(value = "Authorization", required = true) String token){
//
//        try{
//
//            String uName = null;
//
//            if (token.startsWith("Bearer ")) {
//                uName = jwtTokenUtil.extractUsername(token.substring(7)).toLowerCase();
//                System.out.println(uName);
//            }
//            Map<String, String> result = new HashMap<>();
//            boolean status = jwtTokenUtil.validateToken(token.substring(7), uName);
//
//            if(!status){
//                result.put("status", "false");
//                result.put("description", "Invalid Token");
//            }
//
//            String key = uName + "_" + token.substring(7);
//            Optional<UserMast> user = userMastRepository.findByUsername(uName);
//
//            if(user.isPresent()){
//                String password = user.get().getPassword();
//                String storedPassword = redisObjectUtil.getObjectValueAsString(key, "password");
//                Debug.printDebugBoundary();
//                System.out.println("stored password "+ storedPassword);
//                System.out.println("database password "+ password);
//                Debug.printDebugBoundary();
//                if(password.equals(storedPassword)){
//                    result.put("status", "true");
//                    result.put("description", "token is valid");
//                    return result;
//                }else{
//                    result.put("status", "true");
//                    result.put("description", "password is incorrect");
//                    return result;
//                }
//            }
//            result.put("status", "false");
//            result.put("description", "Unauthorized Access");
//            return result;
//
//        }catch(Exception e){
//            Map<String, String> result = new HashMap<>();
//
//            result.put("status", "false");
//            result.put("description", "Invalid Token");
//
//            return result;
//
//        }
//    }
//
//    @GetMapping("/validate-token-new")
//    public ResponseEntity<Map<String, String>> validateTokenNew(@RequestHeader(value = "Authorization") String token) {
//
//        try {
//            String uName = null;
//
//            if (token.startsWith("Bearer ")) {
//                uName = jwtTokenUtil.extractUsername(token.substring(7)).toLowerCase();
//            }
//
//            boolean status = jwtTokenUtil.validateToken(token.substring(7), uName);
//
//            if (!status) {
//                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token");
//            }
//
//            String key = uName + "_" + token.substring(7);
//            Optional<UserMast> user = userMastRepository.findByUsername(uName);
//
//            if (user.isPresent()) {
//                String password = user.get().getPassword();
//                String storedPassword = redisObjectUtil.getObjectValueAsString(key, "password");
//
//                if (password.equals(storedPassword)) {
//                    assert uName != null;
//                    return ResponseEntity.ok(Map.of(
//                            "status", "true",
//                            "description", "token is valid",
//                            "username", uName
//                    ));
//                } else {
//                    return ResponseEntity.ok(Map.of(
//                            "status", "true",
//                            "description", "password is incorrect"
//                    ));
//                }
//            }
//
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized Access");
//
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token", e);
//        }
//    }

//====================================================================================================================================================================
    //private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
    private static final String CONFIG_DIR = System.getProperty("CONFIG_DIR");

    @Autowired
    private DynamicDataSourceBuilder dynamicBuilder;



    @PostMapping("{configName}/centralized-login-new")
    public ResponseEntity<?> centralizedLoginNew(@RequestBody AuthRequestDTO authRequest, @PathVariable("configName") String configName) {



        configName = configName + ".properties";

        Map<String, String> map = new HashMap<>();
        map.put("configName", configName);

        System.out.println(map);
        System.out.println("jai shree ram");
        //redisObjectUtil.saveObject("configName", map, 2, TimeUnit.HOURS);


        File file = new File(CONFIG_DIR, configName);
        if (!file.exists()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Config file not found: " + file.getAbsolutePath()));
        }

        NamedParameterJdbcTemplate npjt = null;

        Properties props = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            props = new Properties();
            props.load(fis);

            DataSource ds = dynamicBuilder.buildDataSource(props);
            JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

            npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

            System.out.println("Database Name kya hai"+dbName);


        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Connection failed: " + ex.getMessage()
            ));
        }

        try {

            String loginMode = DmUtil.getLoginMode(authRequest.getUserName());
            System.out.println(loginMode);
            System.out.println(authRequest);
            //System.out.println(LoginMode.getByCode(1));

            System.out.println("Login Mode "+loginMode);

            if(!loginMode.equals("USERNAME")){
                //String loginMode = LoginMode.getByCode(Integer.parseInt(authRequest.getLoginMode()));


                System.out.println(loginMode);
                Map<String, String> userCredentials = util.getUserCredentials(authRequest.getUserName(), loginMode, configName);

                if(userCredentials != null){

                    authRequest.setUserName(userCredentials.get("user_Name"));
                    //authRequest.setPassword(userCredentials.get("passWd"));

                }else{
                    throw new BadCredentialsException("Invalid email or password");
                }
            }

            System.out.println(authRequest);

            // Authenticate the user
            System.out.println(authRequest.getUserName());
            System.out.println(authRequest.getPassword());

//            if(Integer.parseInt(authRequest.getLoginMode()) == 3){
//                System.out.println("Come as for execution");
//                authenticationManager.authenticate(
//                        new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
//                );
//            }

//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getUserName().toLowerCase()+authRequest.getPassword())
//            );

            Optional<UserMast> userMast = userMastRepository.findByUsername(authRequest.getUserName(), npjt);

            if(userMast.isPresent()){
                String password = userMast.get().getPassword();
                if(!password.equals(generateMD5Hash(authRequest.getUserName().toLowerCase()+authRequest.getPassword()))){
                    throw new BadCredentialsException("bad credential");
                }
            }




        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        System.out.println("User Validated!!!!!!!!!!!!!!!!!!");

        System.out.println("config path : "+System.getProperty("CONFIG_DIR"));


        // Fetch user details
        UserDetails userDetails = userMastService.loadUserByUsername(authRequest.getUserName(), npjt);

        System.out.println("User details  :"+userDetails);

        // Generate JWT token
        String token = jwtTokenUtil.generateToken(userDetails.getUsername().toLowerCase());

        // Optionally, store the token in a cache (e.g., Redis) with the associated username for further validation

        HashMap<String, String> hm = new HashMap<>();

        hm.put("token", token);

//        String companyDB = externalConfig.getCompanyDb();
//        hm.put("companyDB", companyDB);
//        hm.put("username", authRequest.getUserName());


        System.out.println(hm);
        String key = authRequest.getUserName().toLowerCase() + "_"+ token;    //<AP> TBR : I have to rename key to redisKey
        redisObjectUtil.saveObject(key, hm, 60, TimeUnit.MINUTES);
        redisObjectUtil.addFieldToObject(key, "password", generateMD5Hash(authRequest.getUserName().toLowerCase()+authRequest.getPassword()));
        redisObjectUtil.addFieldToObject(key, "configName", configName);
        redisObjectUtil.addFieldToObject(key, "configProperties", props);


//        redisObjectUtil.addFieldToObject(key, "address", "Buxar, Bihar");
//        redisObjectUtil.addFieldToObject(key, "username", authRequest.getUserName());
//        redisObjectUtil.addFieldToObject(key, "loginDate", authRequest.getLoginDate());

        String loginDate = DmUtil.convertUnixTimestampToFormattedDate(authRequest.getLoginDate());
        redisUtil.setRedisUserName(key, authRequest.getUserName());
        redisUtil.setRedisLoginDate(key, loginDate);
        //redisUtil.setRedisDBName(key, companyDB);



        String authKey = authKeyUtil.generateAuthKey();
        authKeyUtil.storeAuthKey(authKey, 60*60*1000);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAuthKey(encryptionDecryptionUtil.encrypt(authKey));
        loginResponseDTO.setToken(token);
        loginResponseDTO.setSecretKey(null);



        return ResponseEntity.ok(loginResponseDTO);

    }




    @GetMapping("/validate-token-new")
    public ResponseEntity<Map<String, String>> validateTokenNew(@RequestHeader(value = "Authorization") String token) {


        String uName = null;
        Debug.printDebugBoundary();
        if (token.startsWith("Bearer ")) {
            uName = jwtTokenUtil.extractUsername(token.substring(7)).toLowerCase();
            System.out.println(uName);
        }

        String key =  uName + "_" + token.substring(7);


        String configName = redisObjectUtil.getObjectValueAsString(key, "configName");

//        Map<String, String> map = new HashMap<>();
//        map.put("configName", configName);
//
//        System.out.println(map);
//        redisObjectUtil.saveObject("configName", map, 2, TimeUnit.HOURS);




        NamedParameterJdbcTemplate npjt = null;

        Debug.printDebugBoundary();
        System.out.println("Stored properties in redis "+redisObjectUtil.getObjectValue(key, "configProperties"));
        Debug.printDebugBoundary();

        //=================================================================================================================
        Object redisValue = redisObjectUtil.getObjectValue(key, "configProperties");

        Properties prop = new Properties();

        if (redisValue instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                prop.put(entry.getKey().toString(), entry.getValue().toString());
            }
        } else {
            throw new IllegalArgumentException("Unexpected object type from Redis: " + redisValue.getClass());
        }
        Debug.printDebugBoundary("\uD83D\uDC96");
        System.out.println(prop);
        Debug.printDebugBoundary("\uD83D\uDC96");

        if(prop.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "Config not found"));
        }


        try{
            DataSource ds = dynamicBuilder.buildDataSource(prop);
            JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

            npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

            System.out.println("Database Name kya hai"+dbName);
        }catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Connection failed: " + ex.getMessage()
            ));
        }

        //========================================================================================================================


//        File file = new File(CONFIG_DIR, configName);
//        if (!file.exists()) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Config file not found: " + file.getAbsolutePath()));
//        }
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            Properties props = new Properties();
//            props.load(fis);
//
//            System.out.println("Config Properties "+props);
//
//            DataSource ds = dynamicBuilder.buildDataSource(props);
//            JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);
//
//            npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
//            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);
//
//            System.out.println("Database Name kya hai"+dbName);
//
//
//        } catch (Exception ex) {
//            return ResponseEntity.internalServerError().body(Map.of(
//                    "error", "Connection failed: " + ex.getMessage()
//            ));
//        }

        try {
            //String uName = null;

            if (token.startsWith("Bearer ")) {
                uName = jwtTokenUtil.extractUsername(token.substring(7)).toLowerCase();
                System.out.println(uName);
            }

            boolean status = jwtTokenUtil.validateToken(token.substring(7), uName);
            System.out.println("status  "+ status);

            if (!status) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token");
            }

            System.out.println("Hare krishna");
            //String key = uName + "_" + token.substring(7);
            Optional<UserMast> user = userMastRepository.findByUsername(uName, npjt);

            if (user.isPresent()) {
                String password = user.get().getPassword();
                String storedPassword = redisObjectUtil.getObjectValueAsString(key, "password");

                if (password.equals(storedPassword)) {
                    assert uName != null;
                    return ResponseEntity.ok(Map.of(
                            "status", "true",
                            "description", "token is valid",
                            "username", uName
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                            "status", "true",
                            "description", "password is incorrect"
                    ));
                }
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized Access");

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token", e);
        }
    }




}
