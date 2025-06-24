package dataman.erp.dmbase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dataman.dmbase.debug.Debug;
import dataman.dmbase.microservice.MsUtil;
import dataman.dmbase.redissessionutil.RedisObjectUtil;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.erp.config.FeignTokenValidationFilter;
import dataman.erp.dmbase.service.UserMastService;
import dataman.erp.config.ExternalConfig;
import dataman.erp.context.PCSDataStore;
import dataman.erp.dmbase.models.CompanyDetailDTO;
import dataman.erp.dmbase.models.PCSData;
import dataman.erp.dmbase.models.PLTDetailDTO;
import dataman.erp.dmbase.models.SiteDetailsDTO;
import dataman.erp.dmbase.service.DmBaseService;
import dataman.erp.dmbase.service.GetSelectedFromList;
//import dataman.erp.jwt.JwtTokenUtil;
import dataman.erp.feign.DatamanERPService;
import dataman.erp.mapper.PCSDataMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/datamanerp")
public class DmBaseController {

    @Autowired
    private DmBaseService dmBaseService;


    @Autowired
    private RedisObjectUtil redisObjectUtil;

    @Autowired
    private ExternalConfig externalConfig;

    @Autowired
    private PCSDataMapper pcsDataMapper;

    @Autowired
    private PCSDataStore pcsDataStore;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GetSelectedFromList getSelectedFromList;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private UserMastService userMastService;

    @Autowired
    private DatamanERPService datamanERPService;

    @Autowired
    private MsUtil msUtil;

    @Operation(summary = "Ping API", description = "Returns pong if alive")
    @GetMapping("/get-plts")
    public ResponseEntity<?> getPlts(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String username){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        String uName = result.get("username");

        String key = uName + "_" + token.substring(7);
        Map<String, PLTDetailDTO> selectedPlt = new HashMap<>();

        Debug.printDebugBoundary();
        dmBaseService.getPlts(username);

        Debug.printDebugBoundary();
        return ResponseEntity.ok(dmBaseService.getPlts(username));

    }


    @PostMapping("/get-initial-pcs-data")
    public ResponseEntity<?> getInitialPCSData(@RequestHeader(value = "Authorization", required = true) String token) {


        String uName = FeignTokenValidationFilter.getCurrentUsername();

        System.out.println("username :"+uName);

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

//        if (!Boolean.parseBoolean(result.get("status"))) {
//            return ResponseEntity
//                    .status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Invalid token"));
//        }
//
//        String uName = result.get("username");

        String key = uName + "_" + token.substring(7);

        System.out.println("User Validated!!!!!!!!!!!!!!!!!!");

        PCSData pcsData = new PCSData();
        pcsData.setUserName(uName);

        pcsData.setUserDescription(userMastService.getUserDescriptionByUserName(uName));
        pcsData.setUserName(uName);
        HashMap<String, String> hm = new HashMap<>();

        hm.put("token", token);
        String companyDB = externalConfig.getCompanyDb();

//        hm.put("companyDB", companyDB);
//        hm.put("username", authRequest.getUserName());


        System.out.println(hm);

        redisObjectUtil.saveObject(key, hm, 60, TimeUnit.MINUTES);


//        redisObjectUtil.addFieldToObject(key, "address", "Buxar, Bihar");
//        redisObjectUtil.addFieldToObject(key, "username", authRequest.getUserName());
//        redisObjectUtil.addFieldToObject(key, "loginDate", authRequest.getLoginDate());



        redisUtil.setRedisDBName(key, companyDB);




        //redisObjectUtil.addFieldToObject(key, "userDescription", authRequest.getUserDescription());
        //redisObjectUtil.addFieldToObject(key, "regionalLanguage", authRequest.getRegionalLanguage());

        Debug.printDebugBoundary();
        System.out.println(redisObjectUtil.getObjectValue(key, "address"));
        Debug.printDebugBoundary();


        ObjectNode responseNode = objectMapper.createObjectNode();




//==========================Get List of companies and plts==========================================

        List<PLTDetailDTO> listOfPlts = pcsDataMapper.mapToPLTDetailDTOList(dmBaseService.getPlts(uName));

        if((listOfPlts != null) && (listOfPlts.size() == 1)){
            int selectedPltCode = listOfPlts.get(0).getPLTCode();
            String compName = listOfPlts.get(0).getComp_Name();
            //redisObjectUtil.addFieldToObject(key, "selectedPlt", listOfPlts.get(0));
            redisUtil.setRedisPltCode(key, String.valueOf(selectedPltCode));
            redisUtil.setRedisCompName(key, compName);
            pcsData.setPltSelected(listOfPlts.get(0));
            pcsData.setLstPLTs(listOfPlts);
            List<CompanyDetailDTO> listOfCompany = pcsDataMapper.mapToCompanyDetailDTOList(dmBaseService.getCompanys(uName, selectedPltCode, companyDB));
            if((listOfCompany != null) && (listOfCompany.size() == 1)){
                //if(true){`
                pcsData.setCSelected(listOfCompany.get(0));
                pcsData.setLstCompanys(listOfCompany);
                String selectedCompCode = String.valueOf(listOfCompany.get(0).getComp_Code());

                redisUtil.setRedisCompCode(key, selectedCompCode);
                //redisObjectUtil.addFieldToObject(key, "selectedCompany", listOfCompany.get(0));
                redisUtil.setRedisTransactionDBName(key, listOfCompany.get(0).getCentralData_Path());
                //List<Map<String, Object>> listOfSites = dmBaseService.getSites(authRequest.getUsername(), externalConfig.getCompanyDb(), selectedCompCode, String.valueOf(selectedPltCode));
                List<SiteDetailsDTO> listOfSites = pcsDataMapper.mapToSiteDetailsDTOList(dmBaseService.getSites(uName, externalConfig.getCompanyDb(), selectedCompCode, String.valueOf(selectedPltCode)));

                if(listOfSites != null && listOfSites.size() == 1){
                    String selectedSiteCode  = String.valueOf(listOfSites.get(0).getCode());// use me in permission query
                    pcsData.setSSelected(listOfSites.get(0));
                    pcsData.setLstSites(listOfSites);
                    String regionalLanguage = listOfSites.get(0).getRegionalLanguage();
                    //redisObjectUtil.addFieldToObject(key, "selectedSites", listOfSites.get(0));

                    redisUtil.setRedisSiteCode(key, selectedSiteCode);
                    redisUtil.setRedisRegionalLanguage(key, regionalLanguage);
                    pcsData.setRegionalLanguage(redisUtil.getRedisRegionalLanguage(key));
                    ResponseEntity.ok("GO TO PERMISSION PAGE");
                }else{
                    pcsData.setLstSites(listOfSites);
                }
            }else{
                //responseNode.putPOJO("listOfCompany", listOfCompany);
                pcsData.setLstCompanys(listOfCompany);
                //return ResponseEntity.ok(listOfCompany);
            }
        }else{
            //responseNode.putPOJO("listOfPlts", listOfPlts);
            pcsData.setLstPLTs(listOfPlts);
            //return ResponseEntity.ok(listOfPlts);
        }

        //redisObjectUtil.addFieldToObject(key, "pcsData", pcsData);

        PCSData pcsData1 = (PCSData) redisObjectUtil.getObjectValue(key, "pcsData");
        System.out.println(pcsData1);

        responseNode.putPOJO("pcsData", pcsData);



        return ResponseEntity.ok(responseNode);

    }

    @PostMapping("/get-companys")
    public ResponseEntity<?> getCompanys(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String pltCode){

        System.out.println("In get company "+pltCode);


        String uName = FeignTokenValidationFilter.getCurrentUsername();

        String key = uName + "_" + token.substring(7);
        Debug.printDebugBoundary("\uD83C\uDF39❤\uD83D\uDC96");
        System.out.println(key);
        Debug.printDebugBoundary("\uD83C\uDF39❤\uD83D\uDC96");

        Debug.printDebugBoundary("❤\uD83D\uDC96\uD83C\uDF39\uD83D\uDE4C✌✌");


//        String dbCompany = (String) redisObjectUtil.getObjectValue(key, "companyDB");
//        String db = redisObjectUtil.getObjectValueAsString(key, "companyDB");

        String dbCompany = redisUtil.getRedisDBName(key);

        //redisObjectUtil.addFieldToObject(key, "selectedPlt", selectedPlt);

        //set pltCode to session
        //redisUtil.setRedisPltCode(key, String.valueOf(selectedPlt.getPLTCode()));
        redisUtil.setRedisPltCode(key, pltCode);

        List<PLTDetailDTO> listOfPlts = pcsDataMapper.mapToPLTDetailDTOList(dmBaseService.getPlts(uName));
        PLTDetailDTO pltDetailDTO = getSelectedFromList.getSelectedPlt(listOfPlts, pltCode);
        //redisUtil.setRedisCompName(key, selectedPlt.getComp_Name());

        //PLTDetailDTO pltObject = (PLTDetailDTO) redisObjectUtil.getObjectValue(key, "selectedPlt");

        //System.out.println("from redis "+pltObject);


        Debug.printDebugBoundary("\uD83C\uDF39❤\uD83D\uDC96");
        System.out.println("username "+uName);
        System.out.println("dbCompany"+ dbCompany);
        Debug.printDebugBoundary("\uD83C\uDF39❤\uD83D\uDC96");



        assert uName != null;

        List<CompanyDetailDTO> companyDetailDTOList = pcsDataMapper.mapToCompanyDetailDTOList(dmBaseService.getCompanys(uName, Integer.parseInt(pltCode), dbCompany));

        //PCSData pcsData = pcsDataStore.get("pcsContextData");

        PCSData pcsData = new PCSData();


        String userDecription = redisObjectUtil.getObjectValueAsString(key, "userDescription");
        String regionalLanguage = redisObjectUtil.getObjectValueAsString(key, "regionalLanguage");
        String loginDate = redisObjectUtil.getObjectValueAsString(key, "loginDate");


        pcsData.setUserName(uName);
        pcsData.setUserDescription(userDecription);
        pcsData.setRegionalLanguage(regionalLanguage);
        pcsData.setLoginDate(loginDate);
        pcsData.setPltSelected(pltDetailDTO);
        pcsData.setLstPLTs(listOfPlts);


//        String pltCode = String.valueOf(selectedPlt.getPLTCode());

        if(companyDetailDTOList != null && companyDetailDTOList.size() == 1){
        //if(true){
            CompanyDetailDTO selectedCompany = companyDetailDTOList.get(0);
            //redisObjectUtil.addFieldToObject(key, "selectedCompany", selectedCompany);
            redisUtil.setRedisCompCode(key, String.valueOf(selectedCompany.getComp_Code()));
            List<SiteDetailsDTO> siteDetailsDTOList = pcsDataMapper.mapToSiteDetailsDTOList(dmBaseService.getSites(uName, externalConfig.getCompanyDb(), String.valueOf(selectedCompany.getComp_Code()), pltCode));
            pcsData.setCSelected(selectedCompany);
            pcsData.setLstCompanys(companyDetailDTOList);
        }else{
            pcsData.setLstCompanys(companyDetailDTOList);
        }

        return ResponseEntity.ok(pcsData);

    }

    @PostMapping("/get-sites")
    public ResponseEntity<?> getSites(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String compCode){




        String uName = FeignTokenValidationFilter.getCurrentUsername();

        String key = uName + "_" + token.substring(7);
        //redisObjectUtil.addFieldToObject(key, "companyCode", selectedCompanyCode);

        //redisObjectUtil.addFieldToObject(key, "selectedCompany", selectedCompany);

        //System.out.println("selected Company "+selectedCompany);
        redisUtil.setRedisCompCode(key, compCode);
        List<PLTDetailDTO> listOfPlts = pcsDataMapper.mapToPLTDetailDTOList(dmBaseService.getPlts(uName));
        PLTDetailDTO pltDetailDTO = getSelectedFromList.getSelectedPlt(listOfPlts, redisUtil.getRedisPltCode(key));


        List<CompanyDetailDTO> companyDetailDTOList = pcsDataMapper.mapToCompanyDetailDTOList(dmBaseService.getCompanys(uName, Integer.parseInt(redisUtil.getRedisPltCode(key)), redisUtil.getRedisDBName(key)));
        CompanyDetailDTO selectedCompany = getSelectedFromList.getSelectedCompany(companyDetailDTOList, compCode);



//        redisUtil.setRedisCompName(key, selectedCompany.getComp_Name());
        redisUtil.setRedisTransactionDBName(key, selectedCompany.getCentralData_Path());

//        PLTDetailDTO pltDetailDTO = (PLTDetailDTO) redisObjectUtil.getObjectValue(key, "selectedPlt");

        String pltCode = String.valueOf(pltDetailDTO.getPLTCode());

        List<SiteDetailsDTO> siteDetailsDTOList = pcsDataMapper.mapToSiteDetailsDTOList(dmBaseService.getSites(uName, externalConfig.getCompanyDb(), String.valueOf(selectedCompany.getComp_Code()), pltCode));


        String userDecription = redisObjectUtil.getObjectValueAsString(key, "userDescription");
        String regionalLanguage = redisObjectUtil.getObjectValueAsString(key, "regionalLanguage");
        String loginDate = redisObjectUtil.getObjectValueAsString(key, "loginDate");


        PCSData pcsData = new PCSData();
        pcsData.setUserName(uName);
        pcsData.setUserDescription(userDecription);
        pcsData.setRegionalLanguage(regionalLanguage);
        pcsData.setLoginDate(loginDate);
        pcsData.setPltSelected(pltDetailDTO);
        pcsData.setLstPLTs(listOfPlts);
        pcsData.setCSelected(selectedCompany);
        pcsData.setLstCompanys(companyDetailDTOList);

        if(siteDetailsDTOList != null && siteDetailsDTOList.size() == 1){
            //redisObjectUtil.addFieldToObject(key, "selectedSite", siteDetailsDTOList.get(0));
            String siteCode = String.valueOf(siteDetailsDTOList.get(0).getCode());
            redisUtil.setRedisSiteCode(key, siteCode);
            redisUtil.setRedisRegionalLanguage(key, siteDetailsDTOList.get(0).getRegionalLanguage());
            pcsData.setSSelected(siteDetailsDTOList.get(0));
            pcsData.setLstSites(siteDetailsDTOList);
        }else{
            pcsData.setLstSites(siteDetailsDTOList);
        }

        return ResponseEntity.ok(pcsData);
    }
}
