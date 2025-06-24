package dataman.erp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import dataman.dmbase.encryptiondecryptionutil.EncryptionDecryptionUtil;
import dataman.dmbase.redissessionutil.AuthKeyUtil;
import dataman.dmbase.redissessionutil.RedisUtil;
import dataman.erp.dmbase.service.UserMastService;
import dataman.erp.dto.TestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/datamanerp")
public class TestController {

    @Autowired
    private UserMastService userMastService;

    @Autowired
    private EncryptionDecryptionUtil encryptionDecryptionUtil;

    @Autowired
    private AuthKeyUtil authKeyUtil;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/rests")
    public ResponseEntity<?> doTests(@RequestBody JsonNode payload, @RequestHeader("authKey") String authKey){


//====================================================================================================================
        try{
            authKey = encryptionDecryptionUtil.decrypt(authKey);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (authKeyUtil.getAuthKey(authKey) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        authKeyUtil.removeAuthKey(authKey);
        HttpHeaders headers = new HttpHeaders();
        String id = authKeyUtil.generateAuthKey();
        authKeyUtil.storeAuthKey(id, 60*60*1000);
        String encryptedAuthKey = null;
        try{
            encryptedAuthKey = encryptionDecryptionUtil.encrypt(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        headers.add("authKey", encryptedAuthKey);
//====================================================================================================================
        String Notes = """ 
                24042025EBG1:-       
                so we have to do bhajan so that we can come to the level of iti matva bhajante mam
                in bhakti sadhna goal and means are same.
                we should serve 100% krishna not dive service of krishna. First of all we need to 
                learn what are the my duty to please shree krishna and in this way one can realize 
                krishna. 
                
                we should do our bhajan properly not only bhojan.
                what is the nature of person who realized the shree krishna or who does proper bhajan:-
                1. He does not involve in prajalpa.
                2. who is always absorb in krishna. thinking of krishna means be aligned with krishna's interest
                one who realized krishna his mind absorb in krishna only. and mind is always absorb in krishna
                and his teachings and how to please krishna.
                
                one who does proper bhajan is always busy in his service and in pleasing krishna.
                
                if you are in proper bhajan then when you offer flower to krishna then that flower will be 
                accepted by krishna who live in golok but rather here is only Goloka.
                
                when someone fully engaged in thinking of krishna will be known as dedicated devotee of krishna.
                and nothing can discourage him even some do anything and whatever good or bad situation came. Even 
                he is always encouraged to serve krishna.
                
                matgat prana:- means fully determined to krishna.
                so our service should be dedicated to krishna only not to any external things. 
        """;


        //System.out.println(userMastService.loadUserByUsername("anup"));
        System.out.println(userMastService.isUserExist("anup"));

        TestDTO testDTO = new TestDTO();
        testDTO.setUserName(payload.get("username").asText());
        testDTO.setPassword(payload.get("password").asText());
        testDTO.setAge(payload.get("age").asInt());
        testDTO.setGender(payload.get("gender").asText());
        return ResponseEntity.ok().headers(headers).body(testDTO);
    }

    @PostMapping("/rest")
    public ResponseEntity<?> doTest(@RequestBody JsonNode payload, @RequestHeader("authKey") String authKey) {

//        ResponseEntity<?> authResponse = authKeyUtil.validateAuthKey(authKey, encryptionDecryptionUtil, 60*60*1000);
        ResponseEntity<?> authResponse = authKeyUtil.validateAuthKey(authKey, 60*60*1000);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            return authResponse;
        }
        HttpHeaders headers = authResponse.getHeaders();

        System.out.println(userMastService.isUserExist("anup"));

        TestDTO testDTO = new TestDTO();
        testDTO.setUserName(payload.get("username").asText());
        testDTO.setPassword(payload.get("password").asText());
        testDTO.setAge(payload.get("age").asInt());
        testDTO.setGender(payload.get("gender").asText());

        return ResponseEntity.ok().headers(headers).body(testDTO);
    }

}
