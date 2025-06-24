package dataman.utility.numsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrdCounterDTO {

    private String code;
    private String name;
    private String numberFormat;
    private String counterFormat;
    private String siteCode;
    private String siteName;
    private String voucherPre;
}
