package dataman.erp.dmbase.service;

import dataman.erp.dmbase.models.CompanyDetailDTO;
import dataman.erp.dmbase.models.PLTDetailDTO;
import dataman.erp.dmbase.models.SiteDetailsDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetSelectedFromList {

    public PLTDetailDTO getSelectedPlt(List<PLTDetailDTO> pltDetailDTOList, String pltCode){

        int PLTCode = Integer.parseInt(pltCode);
        PLTDetailDTO selectedPlt = null;
        for (PLTDetailDTO pltDetailDTO : pltDetailDTOList) {
            if (PLTCode == pltDetailDTO.getPLTCode()) {
                selectedPlt = pltDetailDTO;
            }
        }
        return selectedPlt;
    }

    public CompanyDetailDTO getSelectedCompany(List<CompanyDetailDTO> companyDetailDTOList, String compCode){
        int COMPCode = Integer.parseInt(compCode);
        CompanyDetailDTO selectedCompany = null;
        for(CompanyDetailDTO companyDetailDTO : companyDetailDTOList){
            if(COMPCode == companyDetailDTO.getComp_Code()){
                selectedCompany = companyDetailDTO;
            }
        }
        return selectedCompany;
    }

    public SiteDetailsDTO getSelectedSite(List<SiteDetailsDTO> siteDetailsDTOList, String siteCode){
        int SITECode = Integer.parseInt(siteCode);
        SiteDetailsDTO selectedSite = null;
        for(SiteDetailsDTO siteDetailsDTO : siteDetailsDTOList){
            if(SITECode == siteDetailsDTO.getCode()){
                selectedSite = siteDetailsDTO;
            }
        }
        return selectedSite;
    }
}
