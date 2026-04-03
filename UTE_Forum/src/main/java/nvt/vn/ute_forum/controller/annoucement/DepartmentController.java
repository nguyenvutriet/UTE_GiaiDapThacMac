package nvt.vn.ute_forum.controller.annoucement;


import nvt.vn.ute_forum.dto.DepartmentDTO;
import nvt.vn.ute_forum.model.Department;
import nvt.vn.ute_forum.repository.DepartmentRepo;
import org.springframework.beans.factory.annotation.Autowired; // Thêm cái này
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping; // Nên có cái này
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments") // Thêm cái này cho chuẩn REST
public class DepartmentController {

    @Autowired
    private DepartmentRepo departmentRepository;



    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(
            @PathVariable String id,
            @AuthenticationPrincipal Object principal) { // Kiểm tra đăng nhập

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập bà ơi!");
        }

        return departmentRepository.findById(id)
                .map(dept -> ResponseEntity.ok(new DepartmentDTO(dept))) // Trả về DTO thay vì Entity
                .orElse(ResponseEntity.notFound().build());
    }
}
