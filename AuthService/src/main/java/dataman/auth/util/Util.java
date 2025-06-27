package dataman.auth.util;

import dataman.config.DynamicDataSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;



@Service
public class Util {

//    @Autowired
//    @Qualifier("tirangaCompanyNamedJdbcTemplate")
//    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    //private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
    private static final String CONFIG_DIR = System.getProperty("CONFIG_DIR");

    @Autowired
    private DynamicDataSourceBuilder dynamicBuilder;

    public Map<String, String> getUserCredentials(String username, String loginMode, String configName) {


        //String configName = "myconfig.properties";
        File file = new File(CONFIG_DIR, configName);
        System.out.println("config Name "+configName+" yhi to hai ");


        NamedParameterJdbcTemplate npjt = null;

        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);

            DataSource ds = dynamicBuilder.buildDataSource(props);
            JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

            npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
            String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

            System.out.println("Database Name "+dbName);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if(loginMode.equals("MOBILE")) {

            String sql = "SELECT user_Name, passWd FROM userMast WHERE mobile = :mobile";
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("mobile", username);
            return npjt.queryForObject(sql, params, (rs, rowNum) -> Map.of(
                    "user_Name", rs.getString("user_Name"),
                    "passWd", rs.getString("passWd")
            ));

        }else if(loginMode.equals("EMAIL")){

            String sql = "SELECT user_Name, passWd FROM userMast WHERE eMail = :email";
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("email", username);
            return npjt.queryForObject(sql, params, (rs, rowNum) -> Map.of(
                    "user_Name", rs.getString("user_Name"),
                    "passWd", rs.getString("passWd")
            ));

        }

        return null;
    }
}
