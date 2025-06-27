package dataman.jwt;


import dataman.auth.service.UserMastService;
import dataman.config.DynamicDataSourceBuilder;
import dataman.dmbase.debug.Debug;
import dataman.dmbase.exception.DmException;
import dataman.dmbase.redissessionutil.RedisObjectUtil;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMastService userMastService;

    @Autowired
    private RedisObjectUtil redisObjectUtil;

    //private static final String CONFIG_DIR = "C:/Users/Dataman/myconfigfile";
    private static final String CONFIG_DIR = System.getProperty("CONFIG_DIR");

    @Autowired
    private DynamicDataSourceBuilder dynamicBuilder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        NamedParameterJdbcTemplate npjt = null;

        String requestURI = request.getRequestURI();


        if ("/api/datamanerp/myconfig3/centralized-login-new".equals(requestURI)) {
            System.out.println(">>> Centralized login API called!");
        }

        //String[] ans = "/api/datamanerp/myconfig3/centralized-login-new".substring(1).split("/");

        String[] ans = request.getRequestURI().substring(1).split("/");

        String configName = null;
        if(requestURI.contains("/centralized-login-new")){
            configName = ans[2]+ ".properties";


            File file = new File(CONFIG_DIR, configName);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);

                DataSource ds = dynamicBuilder.buildDataSource(props);
                JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

                npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
                String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

                System.out.println("database Name hai "+dbName);
                System.out.println("jai ho");


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }else{

            String token = authorizationHeader.substring(7);
            username = jwtTokenUtil.extractUsername(token);
            String key = username + "_" + token;
            configName = redisObjectUtil.getObjectValueAsString(key, "configName");
            System.out.println("I have successfully retrived the configName from redis "+configName);

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
                throw new DmException(1000, "Property not loaded from redis");
            }


            try{
                DataSource ds = dynamicBuilder.buildDataSource(prop);
                JdbcTemplate jdbc = dynamicBuilder.buildJdbcTemplate(ds);

                npjt = dynamicBuilder.buildNamedJdbcTemplate(ds);
                String dbName = jdbc.queryForObject("SELECT DB_NAME()", String.class);

                System.out.println("Database Name kya hai"+dbName);
            }catch (Exception ex) {
                throw new DmException(1001, "Error: Connection Failed");
            }
        }

        System.out.println(Arrays.toString(ans));


        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("powerstar");
            try {
                username = jwtTokenUtil.extractUsername(jwt);
                System.out.println("extracted from token "+username);
            } catch (Exception e) {
                logger.error("JWT Token extraction failed: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // Return 403 if token is invalid
                response.getWriter().write("Invalid JWT token");
                return; // Stop further processing
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Fetch user details from CustomUserDetailsService
            //UserDetails userDetails = userMastService.loadUserByUsername(username);
            UserDetails userDetails = userMastService.loadUserByUsername(username, npjt);

            if (jwtTokenUtil.validateToken(jwt, userDetails.getUsername())) {
                // Optionally, store user-specific session or check if the token matches the user's session
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // Token is invalid
                response.getWriter().write("Invalid or expired JWT token");
                return;  // Stop further processing
            }
        }

        // Proceed with the next filter if the token is valid
        filterChain.doFilter(request, response);
    }
}
