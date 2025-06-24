package dataman.utility.numsys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherTypeDTO {

    private String code;
    private String Name;
    private String category;
    private String description;

}
