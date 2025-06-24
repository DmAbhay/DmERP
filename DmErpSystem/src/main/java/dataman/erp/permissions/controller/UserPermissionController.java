package dataman.erp.permissions.controller;

import dataman.dmbase.debug.Debug;
import dataman.dmbase.microservice.MsUtil;
import dataman.dmbase.redissessionutil.RedisObjectUtil;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.erp.config.FeignTokenValidationFilter;
import dataman.erp.dmbase.models.SiteDetailsDTO;
import dataman.erp.dmbase.service.DmBaseService;
import dataman.erp.dmbase.service.GetSelectedFromList;
//import dataman.erp.jwt.JwtTokenUtil;
import dataman.erp.feign.DatamanERPService;
import dataman.erp.mapper.PCSDataMapper;
import dataman.erp.permissions.dto.UserMenu;
import dataman.erp.permissions.dto.UserPermission;
import dataman.erp.permissions.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datamanerp")
public class UserPermissionController {


//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private UserPermissionService userPermissionService;

    @Autowired
    private DmBaseService dmBaseService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PCSDataMapper pcsDataMapper;

    @Autowired
    private GetSelectedFromList getSelectedFromList;

    @Autowired
    private DatamanERPService datamanERPService;

    @Autowired
    private MsUtil msUtil;

    @GetMapping("/get-permissions")
    public ResponseEntity<?> getUserPermission(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String siteCode){

        Debug.printDebugBoundary();
        System.out.println("Site Code : "+siteCode);
        Debug.printDebugBoundary();


//        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));
//
//        if (!Boolean.parseBoolean(result.get("status"))) {
//            return ResponseEntity
//                    .status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Invalid token"));
//        }
//
//        String uName = result.get("username");

        String uName = FeignTokenValidationFilter.getCurrentUsername();

        String key = uName + "_" + token.substring(7);
        String db = redisObjectUtil.getObjectValueAsString(key, "companyDB");

        List<SiteDetailsDTO> siteDetailsDTOList = pcsDataMapper.mapToSiteDetailsDTOList(dmBaseService.getSites(
                redisUtil.getRedisUserName(key),
                redisUtil.getRedisDBName(key),
                redisUtil.getRedisCompCode(key),
                redisUtil.getRedisPltCode(key)
                ));

        SiteDetailsDTO siteDetailsDTO = getSelectedFromList.getSelectedSite(siteDetailsDTOList, siteCode);
        redisUtil.setRedisRegionalLanguage(key, siteDetailsDTO.getRegionalLanguage());
        UserPermission userPermission = userPermissionService.getUserPermission(key, "");
        return ResponseEntity.ok(userPermission);

    }

}
