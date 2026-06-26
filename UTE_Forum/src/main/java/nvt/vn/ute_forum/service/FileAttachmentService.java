package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.model.FileAttachment;
import nvt.vn.ute_forum.model.Request;
import nvt.vn.ute_forum.repository.FileAttachmentRepo;
import nvt.vn.ute_forum.service.pattern.chain_of_responsibility.FileUploadChainFactory;
import nvt.vn.ute_forum.service.pattern.chain_of_responsibility.FileUploadHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepo fileAttachmentRepo;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private FileUploadChainFactory uploadChainFactory;

    // =========================================================
    // REQUEST ATTACHMENTS
    // =========================================================

    public void saveRequestAttachments(Request request, MultipartFile[] attachments) throws IOException {
        if (request == null || request.getId() == null
                || attachments == null || attachments.length == 0) return;
        saveAttachments(attachments, request.getId(),
                (attachment) -> attachment.setRequest(request));
    }

    public void replaceRequestAttachments(Request request, MultipartFile[] attachments) throws IOException {
        if (request == null || request.getId() == null
                || attachments == null || attachments.length == 0) return;
        // Xóa file thật + record DB trước khi upload file mới
        removeRequestAttachments(request.getId());
        saveAttachments(attachments, request.getId(),
                (attachment) -> attachment.setRequest(request));
    }

    public void removeRequestAttachments(String requestId) {
        if (requestId == null || requestId.isBlank()) return;
        List<FileAttachment> attachments = fileAttachmentRepo.findByRequest_Id(requestId);
        if (attachments.isEmpty()) return;

        FileUploadHandler chain = uploadChainFactory.buildChain();
        for (FileAttachment attachment : attachments) {
            // Chain nhận ra URL thuộc Cloudinary hay Local → tự xóa đúng chỗ
            chain.handleDelete(attachment.getFileUrl());
        }
        fileAttachmentRepo.deleteAll(attachments);
    }

    // =========================================================
    // ANNOUNCEMENT ATTACHMENTS
    // =========================================================

    public void saveAnnouncementAttachments(Announcement announcement,
                                            MultipartFile[] attachments) throws IOException {
        if (announcement == null || announcement.getId() == null
                || attachments == null || attachments.length == 0) return;
        saveAttachments(attachments, announcement.getId(),
                (attachment) -> attachment.setAnnouncement(announcement));
    }

    public void removeAnnouncementAttachments(String announcementId,
                                              Collection<String> attachmentIds) {
        if (announcementId == null || announcementId.isBlank()
                || attachmentIds == null || attachmentIds.isEmpty()) return;
        List<FileAttachment> attachments =
                fileAttachmentRepo.findByAnnouncement_IdAndIdIn(announcementId, attachmentIds);
        if (attachments.isEmpty()) return;

        FileUploadHandler chain = uploadChainFactory.buildChain();
        for (FileAttachment attachment : attachments) {
            // ✅ Chain tự xóa đúng chỗ dù là Cloudinary hay Local
            chain.handleDelete(attachment.getFileUrl());
        }
        fileAttachmentRepo.deleteAll(attachments);
    }

    // =========================================================
    // CORE — Upload dùng Chain of Responsibility
    // =========================================================
    private void saveAttachments(MultipartFile[] attachments,
                                 String ownerId,
                                 Consumer<FileAttachment> ownerBinder) throws IOException {
        if (attachments == null || attachments.length == 0) return;

        FileUploadHandler uploadChain = uploadChainFactory.buildChain();

        for (MultipartFile file : attachments) {
            if (file == null || file.isEmpty()) continue;

            String fileUrl;
            try {
                fileUrl = uploadChain.handle(file, ownerId);
            } catch (Exception e) {
                throw new IOException("Không thể lưu tệp đính kèm: " + e.getMessage(), e);
            }

            String originalName = file.getOriginalFilename() == null
                    ? "file"
                    : java.nio.file.Paths.get(file.getOriginalFilename())
                    .getFileName().toString();

            FileAttachment attachment = new FileAttachment();
            attachment.setId(idGeneratorService.nextFileAttachmentId());
            attachment.setFileName(originalName);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize((int) Math.min(file.getSize(), Integer.MAX_VALUE));
            attachment.setCreateAt(LocalDate.now());
            attachment.setFileUrl(fileUrl);
            ownerBinder.accept(attachment);

            fileAttachmentRepo.save(attachment);
        }
    }
}