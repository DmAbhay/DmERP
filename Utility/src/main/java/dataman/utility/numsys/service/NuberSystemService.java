package dataman.utility.numsys.service;

import dataman.dmbase.paging.dto.PagedResponse;
import dataman.dmbase.paging.dto.SearchRequest;
import dataman.dmbase.utils.DmUtil;
import dataman.utility.numsys.dto.GrdCounterDTO;
import dataman.utility.numsys.dto.KeyNameDTO;
import dataman.utility.numsys.dto.NumberSystemDTO;
import dataman.utility.numsys.repository.NumberSystemRepository;
import jakarta.transaction.Transactional;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NuberSystemService {

    @Autowired
    private NumberSystemRepository numberSystemRepository;

    public PagedResponse<Map<String, Object>> fillType(SearchRequest searchRequest, String voucherType){
        return numberSystemRepository.fillType(voucherType, searchRequest);
    }


    public PagedResponse<Map<String, Object>> getNavigator(@RequestBody SearchRequest searchRequest){
        return numberSystemRepository.getNavigator(searchRequest);
    }

    public List<KeyNameDTO> getNumberFormat(){

        List<KeyNameDTO>  keyNameDTOList = new ArrayList<>();
        String[] codes ={"{site}", "{sitesname}", "{year}", "{yearfull}", "{month}", "{day}", "{voucherprefix}", "{voucher}", "{vouchersname}", "{company}", "{counter}"};
        String[] names ={"site", "sitesname", "year", "yearfull", "month", "day", "voucherprefix", "voucher", "vouchersname", "company", "counter"};

        for(int i = 0;i<codes.length;i++){
            KeyNameDTO keyNameDTO = new KeyNameDTO();
            keyNameDTO.setCode(codes[i]);
            keyNameDTO.setName(names[i]);

            keyNameDTOList.add(keyNameDTO);
        }
        return keyNameDTOList;

    }


    public List<KeyNameDTO> getYesNo(){

        List<KeyNameDTO>  keyNameDTOList = new ArrayList<>();
        String[] codes ={"1", "0"};
        String[] names ={"Yes", "No"};

        for(int i = 0;i<codes.length;i++){
            KeyNameDTO keyNameDTO = new KeyNameDTO();
            keyNameDTO.setCode(codes[i]);
            keyNameDTO.setName(names[i]);

            keyNameDTOList.add(keyNameDTO);
        }
        return keyNameDTOList;

    }

    public PagedResponse<Map<String, Object>> getVoucherType(String voucherType, SearchRequest searchRequest){

        PagedResponse<Map<String, Object>> response = numberSystemRepository.getVoucherTypesExcluding(voucherType, searchRequest);

        List<Map<String, Object>> res = response.getData();


        return numberSystemRepository.getVoucherTypesExcluding(voucherType, searchRequest);
    }

    public PagedResponse<Map<String, Object>> getAllVoucherType(SearchRequest searchRequest){

        PagedResponse<Map<String, Object>> response = numberSystemRepository.getAllVoucherTypes(searchRequest);

        return numberSystemRepository.getAllVoucherTypes(searchRequest);
    }

    public List<Map<String, Object>> getVoucherTypeList(String voucherType, SearchRequest searchRequest) {
        PagedResponse<Map<String, Object>> response = numberSystemRepository.getVoucherTypesExcluding(voucherType, searchRequest);

        System.out.println(DmUtil.toPrettyJson(response.getData()));
        return response.getData(); // Only return the list
    }

    public PagedResponse<Map<String, Object>> getCountList(String nsGroup, SearchRequest searchRequest){
        return numberSystemRepository.getCountList(nsGroup, searchRequest);
    }


    public PagedResponse<Map<String, Object>> getSiteList(String pltCode, SearchRequest searchRequest){
        return numberSystemRepository.getSiteList(pltCode, searchRequest);
    }


    public void deleteRecord(String nsGroup) {
        try {
            int count = numberSystemRepository.getCounterCount(nsGroup);
            System.out.println(count);
            if (count < 1) {
                numberSystemRepository.deleteFromNumberSystemFormat(nsGroup);
                numberSystemRepository.deleteFromNumberSystem(nsGroup);
            } else {
                throw new UnsupportedOperationException("Delete related counter.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String saveNuberSystemRecord(NumberSystemDTO numberSystemDTO){
        return numberSystemRepository.saveNumberSystem(numberSystemDTO);
    }


    public void deleteRecordPopUp(GrdCounterDTO linkedUserGrid) {

        try {
            int rowsAffected = numberSystemRepository.executeDeleteCounter(linkedUserGrid);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    @Transactional
    public String insertRecordPopUp(GrdCounterDTO model) {
        String result = model.getCode();

        try {
            int count = numberSystemRepository.fetchCounterExistence(model);
            if (count == 1) {
                numberSystemRepository.updateCounter(model);
            } else if (count > 1) {
                throw new UnsupportedOperationException("This Number Format already exists.");
            } else {
                List<GrdCounterDTO> existingList = numberSystemRepository.fetchCounterListByGroup(model.getCode());
                existingList.add(model); // Add new model to existing list

                numberSystemRepository.deleteCountersByGroup(model.getCode());
                numberSystemRepository.insertCounters(existingList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
