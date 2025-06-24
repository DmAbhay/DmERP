package dataman.utility.userMast;


import dataman.dmbase.exception.DmException;
import dataman.dmbase.paging.dto.PagedResponse;
import dataman.dmbase.paging.dto.SearchRequest;
import dataman.dmbase.paging.service.DmPaging;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.dmbase.utils.DmUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMastServices{

    @Autowired
    @Qualifier("companyJdbcTemplate")
    private JdbcTemplate companyJdbcTemplate;

    @Autowired
    private RedisUtil redisUtil;

    // Method to fetch user details by Code
    public Map<String, Object>
    getUserDetailsWithLinkedAccounts(String userName) {
        Map<String, Object> response = new HashMap<>();

        // Query to fetch user details
        String userDetailsQuery = "SELECT um.User_Name AS userName, um.Description as description, " +
                "um.isAdmin, um.code as userId, um.User_Role as role, um.u_name as uName, um.isdCode as isdCode, " +
                "um.preparedBy as preparedBy, um.email, um.mobile, um.isActive, um.nature, " +
                "um.UserLockTimeOut as lockTimeout " +
                "FROM UserMast um " +
                "LEFT JOIN userNature un ON un.code = um.nature " +
                "WHERE um.User_Name = ? " +
                "ORDER BY um.User_Name";

        // Query to fetch linked account
        String linkedAccountsQuery = "SELECT ula.user_Name AS name, CAST(ula.subCode AS VARCHAR) as subCode, ula.pltCode, ula.departmentCode " +
                "FROM userLinkAccount ula " +
                "WHERE ula.user_name = ?";

        try {
            // Fetch user details
            Map<String, Object> userDetails = companyJdbcTemplate.queryForMap(userDetailsQuery, userName);
            response.put("userDetails", userDetails);

            // Fetch linked account (single map instead of list)
            try {
                Map<String, Object> linkedAccount = companyJdbcTemplate.queryForMap(linkedAccountsQuery, userName);
                response.put("linkedAccount", linkedAccount);
            } catch (Exception ex) {
                response.put("linkedAccount", new HashMap<>());
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user details and linked account for user: " + userName, e);
        }
    }

    public String deleteUserByCode(String code) {
        try {
            // Step 1: Delete from userSite table (remove dependencies)
            String deleteUserSiteQuery = "DELETE FROM userSite WHERE user_Name = ?";
            companyJdbcTemplate.update(deleteUserSiteQuery, code);

            // Step 2: Delete from user_Permission_Web table (remove dependencies)
            String deletePermissionQuery = "DELETE FROM user_Permission_Web WHERE userName = ?";
            companyJdbcTemplate.update(deletePermissionQuery, code);

            // Step 3: Delete from user_Control_Permission_Web table (remove dependencies)
            String deleteControlPermissionQuery = "DELETE FROM user_Control_Permission_Web WHERE userName = ?";
            companyJdbcTemplate.update(deleteControlPermissionQuery, code);

            // Step 4: Delete from userLinkAccount table
            String deletePermissionsQuery = "DELETE FROM userLinkAccount WHERE user_Name = ?";
            companyJdbcTemplate.update(deletePermissionsQuery, code);

            // Step 5: Delete from UserMast table
            String deleteUserQuery = "DELETE FROM UserMast WHERE user_Name = ?";
            int rowsAffected = companyJdbcTemplate.update(deleteUserQuery, code);

            if (rowsAffected > 0) {
                return "User with userName " + code + " deleted successfully.";
            } else {
                return "User with userName " + code + " not found.";
            }
        } catch (Exception e) {
            return "Error deleting user with userName " + code + ": " + e.getMessage();
        }
    }



    public List<Map<String, Object>> getUsers(String prefix, int page, int size) {
        String baseQuery = "SELECT um.User_Name AS userName, um.Description as description, um.User_Role as role, "
                         + "um.email, um.isActive FROM UserMast um "
                         + "WHERE um.User_Name NOT IN ('SUPER', 'SA')";

        // Prefix filter
        if (prefix != null && !prefix.isEmpty()) {
            baseQuery += " AND um.User_Name LIKE '" + prefix + "%'";
        }

        String paginatedQuery = addPagination(baseQuery, page, size);

        return companyJdbcTemplate.queryForList(paginatedQuery);
    }

    private String addPagination(String query, int page, int size) {
        int offset = (page - 1) * size;
        return query + " ORDER BY um.User_Name OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";
    }



    public PagedResponse<Map<String, Object>> getFind(SearchRequest searchRequest, NamedParameterJdbcTemplate npjt, String redisKey) {

        String baseQuery;
        String condition;

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        try {

            //String userName = redisUtil.getRedisUserName(redisKey);
            String userName = "sa";

            if (userName.equalsIgnoreCase("SA") || userName.equalsIgnoreCase("SUPER")) {
                condition = " User_Name NOT IN ('SA') ";
            } else {
                condition = " user_Name = :userName ";
                parameters.addValue("userName", userName);
            }

            baseQuery = "SELECT code, user_Name AS name, eMail, ISNULL(User_Role,'') AS User_Role, description, "
                    + "       CASE WHEN isActive = 1 THEN 'Yes' ELSE 'No' END AS isActive "
                    + "FROM UserMast "
                    + "WHERE " + condition;

            return DmPaging.getFilteredResult(npjt, baseQuery, searchRequest, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void deleteUser(String userName, NamedParameterJdbcTemplate namedJdbcTemplate) throws Exception {

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);

        try {
            // 1. Check if the user has a protected role
            String sql = "SELECT code FROM userMast WHERE User_Role IN (:userName)";
            List<Map<String, Object>> result = namedJdbcTemplate.queryForList(sql, paramMap);
            if (!result.isEmpty()) {
                throw new UnsupportedOperationException("This is system defined. You can not delete it.");
            }

            // 2. Delete from linkVerification
            sql = "DELETE FROM linkVerification WHERE userName = :userName AND keyField = :keyField";
            Map<String, Object> keyParam = new HashMap<>();
            keyParam.put("userName", userName);
            keyParam.put("keyField", "UserMast:" + userName);
            namedJdbcTemplate.update(sql, keyParam);

            // 3. Delete from User_Permission_Web
            sql = "DELETE FROM User_Permission_Web WHERE UserName IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 4. Delete from User_Voucher_Permission
            sql = "DELETE FROM User_Voucher_Permission WHERE User_Name IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 5. Delete from user_control_permission_web
            sql = "DELETE FROM user_control_permission_web WHERE UserName IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 6. Delete from UserSite
            sql = "DELETE FROM UserSite WHERE User_Name IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 7. Delete from userLinkAccount
            sql = "DELETE FROM userLinkAccount WHERE User_Name IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 8. Delete from userMobileAppPermisson
            sql = "DELETE FROM userMobileAppPermisson WHERE user_Name IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);

            // 9. Delete from UserMast
            sql = "DELETE FROM UserMast WHERE User_Name IN (:userName)";
            namedJdbcTemplate.update(sql, paramMap);



        } catch (DmException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException(e.getMessage());
        }
    }


}
