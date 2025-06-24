package dataman.utility.numsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NumberFormatDTO {
    private String siteCode;
    private String numberFormat;
    private String counterFormat;
    private String isYearWiseCode;
    private String isSiteWiseCode;
}