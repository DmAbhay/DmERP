package dataman.utility.userMast;

import dataman.dmtoolkit.dto.UserMastModel;
import dataman.dmtoolkit.server.UserMastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dataman/utility")
public class UserMastControllers {

	@Autowired
	private UserMastService userMastService;

	@Autowired
	@Qualifier("companyJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@PostMapping("/save")
	public ResponseEntity<?> saveUser(@RequestBody UserMastModel userDto) {
		try {
			userDto.setPltCode(1);
			userDto.setSubCode(userDto.getSubCode() != null ? userDto.getSubCode() : 0);
			userDto.setDepartmentCode(userDto.getDepartmentCode() != null ? userDto.getDepartmentCode() : 0);
			userDto.setAllowedDepartmentCodeList(
					userDto.getAllowedDepartmentCodeList() != null ? userDto.getAllowedDepartmentCodeList() : "0");
	        userDto.setLinkedUser(userDto.getLinkedUser() != null ? userDto.getLinkedUser() : List.of());


			String responseMessage = userMastService.saveOrUpdate(userDto, jdbcTemplate);

			// Return a success response
			return ResponseEntity.ok(responseMessage);
		} catch (RuntimeException e) {
			// If there is an error, return a bad request response with the error message
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
