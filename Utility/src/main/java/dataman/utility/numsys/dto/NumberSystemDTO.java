package dataman.utility.numsys.dto;

import lombok.Data;

import java.util.List;

@Data
public class NumberSystemDTO {
    private String nsGroup;
    private String vType;
    private String uName;
    private List<NumberFormatDTO> grdNumberFormat;
    private List<IncludVocherTypeDTO> grdVType;
}





