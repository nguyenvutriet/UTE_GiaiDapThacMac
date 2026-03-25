package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.FileAttachment;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.repository.FileAttachmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepo fileAttachmentRepo;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public void saveRequestAttachments(Request request, MultipartFile[] attachments) throws IOException {
        if (request == null || request.getId() == null || attachments == null || attachments.length == 0) {
            return;
        }

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);

        for (MultipartFile file : attachments) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String originalName = file.getOriginalFilename() == null ? "file" : Paths.get(file.getOriginalFilename()).getFileName().toString();
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }

            String storedFileName = request.getId() + "_" + System.nanoTime() + extension;
            Path target = uploadRoot.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            FileAttachment attachment = new FileAttachment();
            attachment.setId("FA_" + System.nanoTime());
            attachment.setFileName(originalName);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize((int) Math.min(file.getSize(), Integer.MAX_VALUE));
            attachment.setCreateAt(LocalDate.now());
            attachment.setFileUrl("/uploads/" + storedFileName);
            attachment.setRequest(request);

            fileAttachmentRepo.save(attachment);
        }
    }

}
