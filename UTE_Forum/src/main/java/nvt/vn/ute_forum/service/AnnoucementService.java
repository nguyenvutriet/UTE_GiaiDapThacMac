package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.StaffAnnouncementCardDTO;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnnoucementService {

    private static final Pattern DATE_IN_ID_PATTERN = Pattern.compile("(\\d{8})");
    private static final DateTimeFormatter SOURCE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TARGET_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private AnnouncementRepo announcementRepo;

    public Page<StaffAnnouncementCardDTO> getAnnouncementCards(Pageable pageable) {
        return announcementRepo.findAllByOrderByIdDesc(pageable)
                .map(this::toCardDTO);
    }

    public Page<StaffAnnouncementCardDTO> searchAnnouncementCards(Pageable pageable,
                                                                  String keyword,
                                                                  String departmentId) {
        String normalizedKeyword = normalizeFilterValue(keyword);
        String normalizedDepartmentId = normalizeFilterValue(departmentId);

        return announcementRepo.searchAnnouncements(normalizedKeyword, normalizedDepartmentId, pageable)
                .map(this::toCardDTO);
    }

    private StaffAnnouncementCardDTO toCardDTO(Announcement announcement) {
        StaffAnnouncementCardDTO dto = new StaffAnnouncementCardDTO();
        dto.setId(announcement.getId());
        dto.setTitle((announcement.getTitle() == null || announcement.getTitle().isBlank()) ? "Thong bao" : announcement.getTitle());
        dto.setContent(announcement.getContent() == null ? "" : announcement.getContent());

        if (announcement.getUser() != null) {
            dto.setAuthorName(announcement.getUser().getFullName());
            if (announcement.getUser().getDepartment() != null) {
                dto.setDepartmentName(announcement.getUser().getDepartment().getName());
            }
        }

        if (dto.getAuthorName() == null || dto.getAuthorName().isBlank()) {
            dto.setAuthorName("Phong ban");
        }
        if (dto.getDepartmentName() == null || dto.getDepartmentName().isBlank()) {
            dto.setDepartmentName("Phong ban");
        }

        dto.setDateLabel(resolveDateLabel(announcement.getId()));
        return dto;
    }

    private String resolveDateLabel(String announcementId) {
        if (announcementId == null || announcementId.isBlank()) {
            return "--/--/----";
        }

        Matcher matcher = DATE_IN_ID_PATTERN.matcher(announcementId);
        if (!matcher.find()) {
            return "--/--/----";
        }

        try {
            LocalDate parsed = LocalDate.parse(matcher.group(1), SOURCE_DATE_FORMAT);
            return parsed.format(TARGET_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return "--/--/----";
        }
    }

    private String normalizeFilterValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
