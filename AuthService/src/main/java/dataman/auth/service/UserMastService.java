package dataman.auth.service;

import dataman.auth.dto.UserMast;
import dataman.auth.repository.UserMastRepository;
import dataman.config.DynamicDataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

//@Service
//public class UserMastService implements UserDetailsService {
//
//    @Autowired
//    private UserMastRepository userMastRepository;
//
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;  // Inject PasswordEncoder
//
//    @Autowired
//    private DynamicDataSourceBuilder dynamicDataSourceBuilder;
//
//    private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
//
//
////    @Override
////    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
////
////        System.out.println("Username sent correctly in service layer");
////        UserMast user = userMastRepository.findByUsername(username)
////                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
////
////        return User.builder()
////                .username(user.getUsername())
////                .password(user.getPassword())
////                .roles("USER")
////                .build();
////
////    }
//
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//
//
////        String configName = "myconfig3.properties";
////
////        Map<String, String> map = new HashMap<>();
////        map.put("configName", configName);
////
////        System.out.println(map);
////
////
////
////        File file = new File(CONFIG_DIR, configName);
////
////
////        NamedParameterJdbcTemplate npjt = null;
////
////        try (FileInputStream fis = new FileInputStream(file)) {
////            Properties props = new Properties();
////            props.load(fis);
////
////            DataSource ds = dynamicDataSourceBuilder.buildDataSource(props);
////            JdbcTemplate jdbc = dynamicDataSourceBuilder.buildJdbcTemplate(ds);
////
////            npjt = dynamicDataSourceBuilder.buildNamedJdbcTemplate(ds);
////            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);
////
////            System.out.println("Database Name "+dbName);
////
////
////        } catch (Exception ex) {
////            ex.printStackTrace();
////        }
//
//
////        //System.out.println("Username sent correctly in service layer");
////        System.out.println("Username sent correctly in service layer");
////        UserMast user = userMastRepository.findByUsername(username, npjt)
////                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        UserMast user = new UserMast();
//
//        return User.builder()
//                .username(user.getUsername())
//                .password(user.getPassword())
//                .roles("USER")
//                .build();
//
//    }
//
//    public boolean isUserExist(String username){
//
//        String configName = "myconfig3.properties";
//
//        Map<String, String> map = new HashMap<>();
//        map.put("configName", configName);
//
//        System.out.println(map);
//
//
//
//        File file = new File(CONFIG_DIR, configName);
//
//
//        NamedParameterJdbcTemplate npjt = null;
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            Properties props = new Properties();
//            props.load(fis);
//
//            DataSource ds = dynamicDataSourceBuilder.buildDataSource(props);
//            JdbcTemplate jdbc = dynamicDataSourceBuilder.buildJdbcTemplate(ds);
//
//            npjt = dynamicDataSourceBuilder.buildNamedJdbcTemplate(ds);
//            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);
//
//            System.out.println("Database Name "+dbName);
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return userMastRepository.existsByUsername(username, npjt);
//    }
//    public String getUserDescriptionByUserName(String userName){
//
//        String configName = "myconfig3.properties";
//
//        Map<String, String> map = new HashMap<>();
//        map.put("configName", configName);
//
//        System.out.println(map);
//
//
//
//        File file = new File(CONFIG_DIR, configName);
//
//
//        NamedParameterJdbcTemplate npjt = null;
//
//        try (FileInputStream fis = new FileInputStream(file)) {
//            Properties props = new Properties();
//            props.load(fis);
//
//            DataSource ds = dynamicDataSourceBuilder.buildDataSource(props);
//            JdbcTemplate jdbc = dynamicDataSourceBuilder.buildJdbcTemplate(ds);
//
//            npjt = dynamicDataSourceBuilder.buildNamedJdbcTemplate(ds);
//            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);
//
//            System.out.println("Database Name "+dbName);
//
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//
//        return userMastRepository.getDescriptionByUsername(userName, npjt);
//    }
//
////=========================================================================================================================================================
//
//
//    public UserDetails loadUserByUsername(String username, NamedParameterJdbcTemplate npjt) throws UsernameNotFoundException {
//
//        System.out.println("Username sent correctly in service layer in custom loadByUsername");
//        UserMast user = userMastRepository.findByUsername(username, npjt)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        return User.builder()
//                .username(user.getUsername())
//                .password(user.getPassword())
//                .roles("USER")
//                .build();
//
//    }
//
//
//
//
//
//
//
//    public boolean isUserExist(String username, NamedParameterJdbcTemplate npjt){
//        return userMastRepository.existsByUsername(username, npjt);
//    }
//    public String getUserDescriptionByUserName(String userName, NamedParameterJdbcTemplate npjt){
//        return userMastRepository.getDescriptionByUsername(userName, npjt);
//    }
//
//
//
//
//
//}



@Service
public class UserMastService  {

    @Autowired
    private UserMastRepository userMastRepository;


//    @Autowired
//    private PasswordEncoder passwordEncoder;  // Inject PasswordEncoder

    private final PasswordEncoder passwordEncoder;

    public UserMastService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private DynamicDataSourceBuilder dynamicDataSourceBuilder;

    //private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
    //private static final String CONFIG_DIR = System.getProperty("CONFIG_DIR");




//=========================================================================================================================================================


    public UserDetails loadUserByUsername(String username, NamedParameterJdbcTemplate npjt) throws UsernameNotFoundException {

        System.out.println("Username sent correctly in service layer in custom loadByUsername");
        UserMast user = userMastRepository.findByUsername(username, npjt)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();

    }

    public boolean isUserExist(String username, NamedParameterJdbcTemplate npjt){
        return userMastRepository.existsByUsername(username, npjt);
    }
    public String getUserDescriptionByUserName(String userName, NamedParameterJdbcTemplate npjt){
        return userMastRepository.getDescriptionByUsername(userName, npjt);
    }

}
