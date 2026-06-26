package nvt.vn.ute_forum.controller.admin;

import nvt.vn.ute_forum.dto.CommentReportDTO;
import nvt.vn.ute_forum.model.Comment;
import nvt.vn.ute_forum.model.Users;
import nvt.vn.ute_forum.model.strategy.commentreport.NotViolationStrategy;
import nvt.vn.ute_forum.model.strategy.commentreport.ReportActionContext;
import nvt.vn.ute_forum.model.strategy.commentreport.ViolationStrategy;
import nvt.vn.ute_forum.repository.CommentRepo;
import nvt.vn.ute_forum.repository.CommentReportRepo;
import nvt.vn.ute_forum.service.CommentReportService;
import nvt.vn.ute_forum.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class CommentReportController {

    @Autowired
    private CommentReportService reportService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private CommentReportRepo reportRepo;

//    @PostMapping("/create")
//    public ResponseEntity<?> createReport(@RequestBody CommentReportDTO dto) {
//
//        try {
//            Comment comment = commentRepo.findById(dto.getCommentId())
//                    .orElse(null);
//
//            if (comment == null)
//                return ResponseEntity.badRequest().body("Comment not found");
//
//            Users student = usersService.getCurrentUser(); // người báo cáo
//
//            CommentReport report = new CommentReport();
//            report.setId(UUID.randomUUID().toString());
//            report.setComment(comment);
//            report.setStudent(student);
//            report.setReason(dto.getReason());
//            report.setStatus("pending");
//            report.setCreatedAt(LocalDateTime.now());
//            report.setAdmin(null); // chưa ai xử lý
//
//            reportRepo.save(report);
//
//            return ResponseEntity.ok("SUCCESS");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("ERROR: " + e.getMessage());
//        }
//    }

    @GetMapping
    public String viewReports(Model model,
                              Principal principal,
                              @RequestParam(value="keyword", required=false) String keyword,
                              @RequestParam(value="status", required=false) String status) {

        Users admin = null;
        if (principal != null) {
            admin = usersService.getByEmail(principal.getName());
        }

        List<CommentReportDTO> list = reportService.findAllDTO();
        list = reportService.filter(list, keyword, status);

        model.addAttribute("user", admin);
        model.addAttribute("reports", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "admin/comment-report";
    }


//    @PostMapping("/{id}/resolve")
//    @ResponseBody
//    public String resolveReport(@PathVariable String id, Principal principal) {
//
//        Users admin = usersService.getByEmail(principal.getName());
//
//        reportService.updateStatus(id, admin);
//
//        return "OK";
//    }

    @PostMapping("/{id}/resolve")
    @ResponseBody
    public ResponseEntity<String> resolveReport(
            @PathVariable String id,
            @RequestParam("action") String action, // "violation" hoặc "not_violation"
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập!");
        }

        Users admin = usersService.getByEmail(principal.getName());
        if (admin == null) {
            return ResponseEntity.status(401).body("Người dùng không tồn tại!");
        }

        return reportService.findById(id).map(report -> {

            ReportActionContext context = new ReportActionContext();

            if ("violation".equalsIgnoreCase(action)) {

                context.setStrategy(
                        new ViolationStrategy(reportService)
                );

            }
            else if ("not_violation".equalsIgnoreCase(action)) {

                context.setStrategy(
                        new NotViolationStrategy(reportService)
                );

            }
            else{
                return ResponseEntity.badRequest().body("Hành động không hợp lệ");
            }

            context.execute(report, admin);

            return ResponseEntity.ok("Xử lý thành công");

        }).orElse(ResponseEntity.badRequest().body("Report không tồn tại"));
    }
}