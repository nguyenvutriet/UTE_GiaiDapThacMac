package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.AnnouncementResponse;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnoucementService {

    @Autowired
    private AnnouncementRepo announcementRepo;
    public AnnouncementResponse mapToDTO(Announcement ann) {
        AnnouncementResponse dto = new AnnouncementResponse();
        dto.setId(ann.getId());
        dto.setTitle(ann.getTitle());
        dto.setContent(ann.getContent());

        // Format ngày: 20/12/2025
        if (ann.getDate() != null) {
            dto.setDate(ann.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        // Lấy tên phòng ban (Đi từ Announcement -> User -> Department)
        if (ann.getUser() != null && ann.getUser().getDepartment() != null) {
            dto.setCreatorName(ann.getUser().getDepartment().getName());
        }

        // Map danh sách file đính kèm
        if (ann.getAttachments() != null) {
            List<AnnouncementResponse.FileDTO> fileDTOs = ann.getAttachments().stream()
                    .map(file -> new AnnouncementResponse.FileDTO(
                            file.getFileName(),
                            file.getFileUrl(),
                            file.getFileType()
                    ))
                    .collect(Collectors.toList());
            dto.setAttachments(fileDTOs);
        }

        return dto;
    }

}
