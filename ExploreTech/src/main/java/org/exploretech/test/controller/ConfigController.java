package org.exploretech.test.controller;

import dataman.dmbase.redissessionutil.RedisObjectUtil;
import org.exploretech.config.DynamicDataSourceBuilder;
import org.exploretech.config.ExternalConfigAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ExternalConfigAuthService configService;

    @Autowired
    private DynamicDataSourceBuilder dynamicBuilder;

    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @GetMapping("/load-properties")
    public ResponseEntity<?> loadProperties(@RequestParam("configName") String configName) {

        configName = configName + ".properties";

        System.out.println(configService.getProperty(configName, "sqlHostName"));

        Properties props = configService.getProperties(configName);
        if (props == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Config not found: " + configName));
        }

        DataSource ds = dynamicBuilder.buildDataSource(props);
        JdbcTemplate jdbcTemplate = dynamicBuilder.buildJdbcTemplate(ds);
        NamedParameterJdbcTemplate namedJdbcTemplate = dynamicBuilder.buildNamedJdbcTemplate(jdbcTemplate);

        // Test the connection
        try {
            String dbName = jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
            return ResponseEntity.ok(Map.of(
                    "message", "Connected to DB: " + dbName,
                    "host", props.getProperty("sqlHostName")
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Connection failed: " + e.getMessage()));
        }
    }




    @GetMapping("/load-properties-new")
    public ResponseEntity<?> loadPropertiesNew(@RequestParam("configName") String configName) {
        String configFile = configName + ".properties";




        Properties props = configService.getProperties(configFile);


        if (props == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No such config: " + configFile));
        }

        try {
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
                    "error", "Failed to connect: " + ex.getMessage()
            ));
        }
    }


    private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";

    @GetMapping("/load-properties-new-new")
    public ResponseEntity<?> loadPropertiesNewNew(@RequestParam("configName") String configName) {
        configName = configName + ".properties";



        Map<String, String> map = new HashMap<>();

        map.put("configName", configName);

        redisObjectUtil.saveObject("configName", map, 2, TimeUnit.HOURS);


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
