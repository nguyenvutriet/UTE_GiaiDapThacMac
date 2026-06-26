package nvt.vn.ute_forum.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileDownloadController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/files/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("url") String fileUrl,
                                               @RequestParam("name") String fileName) {
        try {
            byte[] fileBytes;

            // 1. KIỂM TRA NGUỒN GỐC FILE ĐỂ LẤY DỮ LIỆU MẢNG BYTE
            if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                // Nếu là file trên Cloudinary -> Tải dữ liệu qua mạng về Server
                fileBytes = restTemplate.getForObject(fileUrl, byte[].class);
            } else {
                // Nếu là file ở Local (Ví dụ: "/uploads/user1/tailieu.pdf")
                // Chuyển đường dẫn tương đối thành Path và đọc trực tiếp từ ổ cứng Server
                String uploadDir = "uploads"; // Thư mục cấu hình lưu file local của bạn
                String relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 9);
                Path path = Paths.get(uploadDir, relativePath);

                if (!Files.exists(path)) {
                    return ResponseEntity.notFound().build();
                }
                fileBytes = Files.readAllBytes(path);
            }

            // 2. MÃ HÓA TÊN FILE TRÁNH LỖI PHÔNG TIẾNG VIỆT
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            // 3. THIẾT LẬP HEADER TRẢ VỀ CHUẨN ĐỊNH DẠNG PDF
            HttpHeaders headers = new HttpHeaders();
            if (fileName.toLowerCase().endsWith(".pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            headers.setContentDispositionFormData("attachment", encodedFileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}