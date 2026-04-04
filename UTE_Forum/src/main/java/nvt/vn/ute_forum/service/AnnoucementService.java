package nvt.vn.ute_forum.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import nvt.vn.ute_forum.dto.AnnouncementResponse;
import nvt.vn.ute_forum.model.Announcement;
import nvt.vn.ute_forum.repository.AnnouncementRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate; // ĐÚNG
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
                            file.getId(),
                            file.getFileName(),
                            file.getFileUrl(),
                            file.getFileType()
                    ))
                    .collect(Collectors.toList());
            dto.setAttachments(fileDTOs);
        }

        return dto;
    }

    public class AnnouncementSpecifications {
        public static Specification<Announcement> filterAnnouncements(
                String departmentId,
                LocalDateTime startDate,
                LocalDateTime endDate) {

            return new Specification<Announcement>() {

                @Override
                public Predicate toPredicate(Root<Announcement> root,
                                             CriteriaQuery<?> query,
                                             CriteriaBuilder cb) {

                    List<Predicate> predicates = new ArrayList<>();

                    // 1. Lọc theo department
                    if (departmentId != null && !departmentId.isEmpty()) {
                        Predicate departmentPredicate = cb.equal(
                                root.get("user").get("department").get("id"),
                                departmentId
                        );
                        predicates.add(departmentPredicate);
                    }

                    // 2. Lọc theo startDate
                    if (startDate != null) {
                        Predicate startDatePredicate = cb.greaterThanOrEqualTo(
                                root.get("date"),
                                startDate
                        );
                        predicates.add(startDatePredicate);
                    }

                    // 3. Lọc theo endDate
                    if (endDate != null) {
                        Predicate endDatePredicate = cb.lessThanOrEqualTo(
                                root.get("date"),
                                endDate
                        );
                        predicates.add(endDatePredicate);
                    }


                    return cb.and(predicates.toArray(new Predicate[0]));
                }
            };

        }
    }

}
