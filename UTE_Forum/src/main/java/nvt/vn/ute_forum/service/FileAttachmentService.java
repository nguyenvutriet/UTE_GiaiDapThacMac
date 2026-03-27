package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.FileAttachment;
import nvt.vn.ute_forum.model.Announcement;
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
import java.util.Collection;
import java.util.List;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepo fileAttachmentRepo;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    public void saveRequestAttachments(Request request, MultipartFile[] attachments) throws IOException {
        if (request == null || request.getId() == null || attachments == null || attachments.length == 0) {
            return;
        }

        saveAttachments(attachments, request.getId(), (attachment) -> attachment.setRequest(request));
    }

    public void saveAnnouncementAttachments(Announcement announcement, MultipartFile[] attachments) throws IOException {
        if (announcement == null || announcement.getId() == null || attachments == null || attachments.length == 0) {
            return;
        }

        saveAttachments(attachments, announcement.getId(), (attachment) -> attachment.setAnnouncement(announcement));
    }

    public void removeAnnouncementAttachments(String announcementId, Collection<String> attachmentIds) {
        if (announcementId == null || announcementId.isBlank() || attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }

        List<FileAttachment> attachments = fileAttachmentRepo.findByAnnouncement_IdAndIdIn(announcementId, attachmentIds);
        if (attachments.isEmpty()) {
            return;
        }

        for (FileAttachment attachment : attachments) {
            deleteStoredFileSafely(attachment.getFileUrl());
        }

        fileAttachmentRepo.deleteAll(attachments);
    }

    private void deleteStoredFileSafely(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String relativePath = fileUrl;
        if (relativePath.startsWith("/uploads/")) {
            relativePath = relativePath.substring("/uploads/".length());
        }

        try {
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = uploadRoot.resolve(relativePath).normalize();

            if (!target.startsWith(uploadRoot)) {
                return;
            }

            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Keep DB cleanup even if file is already missing on disk.
        }
    }

    private void saveAttachments(MultipartFile[] attachments, String ownerId, java.util.function.Consumer<FileAttachment> ownerBinder) throws IOException {
        if (attachments == null || attachments.length == 0) {
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

            String storedFileName = ownerId + "_" + System.nanoTime() + extension;
            Path target = uploadRoot.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            FileAttachment attachment = new FileAttachment();
            attachment.setId(idGeneratorService.nextFileAttachmentId());
            attachment.setFileName(originalName);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize((int) Math.min(file.getSize(), Integer.MAX_VALUE));
            attachment.setCreateAt(LocalDate.now());
            attachment.setFileUrl("/uploads/" + storedFileName);
            ownerBinder.accept(attachment);

            fileAttachmentRepo.save(attachment);
        }
    }

}
