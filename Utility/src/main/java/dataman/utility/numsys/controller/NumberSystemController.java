package dataman.utility.numsys.controller;

import dataman.dmbase.microservice.MsUtil;
import dataman.dmbase.paging.dto.PagedResponse;
import dataman.dmbase.paging.dto.SearchRequest;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.utility.feign.DatamanERPService;
import dataman.utility.numsys.dto.DmResult;
import dataman.utility.numsys.dto.ErrorDetails;
import dataman.utility.numsys.dto.InfoDetails;
import dataman.utility.numsys.dto.NumberSystemDTO;
import dataman.utility.numsys.repository.NumberSystemRepository;
import dataman.utility.numsys.service.NuberSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dataman/utility")
public class NumberSystemController {

    @Autowired
    private NuberSystemService nuberSystemService;

    @Autowired
    private NumberSystemRepository numberSystemRepository;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MsUtil msUtil;

    @Autowired
    private DatamanERPService datamanERPService;

    @PostMapping("/get-navigator")
    public ResponseEntity<?> getNavigator(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(nuberSystemService.getNavigator(searchRequest));
    }


    @PostMapping("/fill-Type")
    public ResponseEntity<?> fillType(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest, @RequestParam String voucherType){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(nuberSystemService.fillType(searchRequest, voucherType));
    }

    @GetMapping("/get-number-format")
    public ResponseEntity<?> getNumberFormat(@RequestHeader(value = "Authorization", required = true) String token){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(nuberSystemService.getNumberFormat());
    }

//    @GetMapping("/get-yes-no")
//    public ResponseEntity<?> getYesNo(@RequestHeader(value = "Authorization", required = true) String token){
//        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));
//
//        if (!Boolean.parseBoolean(result.get("status"))) {
//            return ResponseEntity
//                    .status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Invalid token"));
//        }
//        return ResponseEntity.ok(nuberSystemService.getYesNo());
//    }



    @GetMapping("/get-yes-no")
    public ResponseEntity<?> getYesNo(){



        return ResponseEntity.ok(nuberSystemService.getYesNo());
    }

    @PostMapping("/get-voucher-type")
    public ResponseEntity<?> getVoucherType(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest, @RequestParam("voucherType") String voucherType){
        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }
        //nuberSystemService.getVoucherTypeList(voucherType, searchRequest);
        return ResponseEntity.ok(nuberSystemService.getVoucherType(voucherType, searchRequest));
    }

    @PostMapping("/get-all-voucher-type")
    public ResponseEntity<?> getAllVoucherType(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        DmResult dmResult = new DmResult();
        ErrorDetails error = new ErrorDetails();
        List<ErrorDetails> errorDetails = new ArrayList<>();
        InfoDetails infoDetails = new InfoDetails();
        List<InfoDetails> infoDetailsList = new ArrayList<>();


        int[] arr = {1, 2, 3};

        try{
            int a = arr[4];
        }catch (ArrayIndexOutOfBoundsException e){
            error  = new ErrorDetails();
            error.setErrorCode("1");
            error.setDescription(e.getMessage());
            errorDetails.add(error);
        }

        if (!Boolean.parseBoolean(result.get("status"))) {

            dmResult.setStatus("FAILURE");
            dmResult.setData(Map.of("message", "Invalid token"));

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(dmResult);
        }

        PagedResponse<Map<String, Object>> response = nuberSystemService.getAllVoucherType(searchRequest);

        dmResult.setData(response);

        try{
            int a = arr[5];
        }catch (ArrayIndexOutOfBoundsException e){
            error = new ErrorDetails();
            error.setErrorCode("1");
            error.setDescription(e.getMessage());
            errorDetails.add(error);
        }
        dmResult.setError(errorDetails);

        return ResponseEntity.ok(dmResult);
    }

    @PostMapping("/get-count-list")
    public ResponseEntity<?> getCountList(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest, @RequestParam String nsGroup){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(nuberSystemService.getCountList(nsGroup, searchRequest));
    }

    @PostMapping("/get-site-list")
    public ResponseEntity<?> getSiteList(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody SearchRequest searchRequest){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        String uName = result.get("username");

        String redisKey = uName + "_" + token.substring(7);

        //String redisKey = util.getRedisKey(token);
        String pltCode = redisUtil.getRedisPltCode(redisKey);

        if(pltCode == null){
            pltCode = "1";
        }
        return ResponseEntity.ok(nuberSystemService.getSiteList(pltCode, searchRequest));
    }

    @DeleteMapping("/delete-number-system")
    public ResponseEntity<?> deleteRecord(@RequestHeader(value = "Authorization", required = true) String token, @RequestParam String nsGroup){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        nuberSystemService.deleteRecord(nsGroup);
        return ResponseEntity.ok("Record Deleted Successfully");
    }


    @PostMapping("/save-number-system")
    public ResponseEntity<?> saveNumberSystem(@RequestHeader(value = "Authorization", required = true) String token, @RequestBody NumberSystemDTO numberSystemDTO){


        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        String uName = result.get("username");

        String redisKey = uName + "_" + token.substring(7);

        String userName =  uName;
        //String userName =  redisUtil.getRedisUserName(util.getRedisKey(token));
        if(userName == null){
            userName = "abhay";
        }
        numberSystemDTO.setUName(userName);
        return ResponseEntity.ok(nuberSystemService.saveNuberSystemRecord(numberSystemDTO));
    }

    @GetMapping("/csv-file")
    public ResponseEntity<?> exportCSV(@RequestHeader(value = "Authorization", required = true) String token){

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        return ResponseEntity.ok(numberSystemRepository.exportDataToCSV());
    }


    @GetMapping("/export-csv")
    public ResponseEntity<?> exportCSVs(@RequestHeader(value = "Authorization", required = true) String token) {

        Map<String, String> result = msUtil.getGenericResponse(() -> datamanERPService.validateTokenNew(token));

        if (!Boolean.parseBoolean(result.get("status"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid token"));
        }

        File csvFile = numberSystemRepository.exportDataToCSVFiles();
        if (csvFile == null || !csvFile.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(csvFile));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + csvFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvFile.length())
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping("/test-dmResult")
//    public ResponseEntity<?> test(){
//
//    }

}
