package nvt.vn.ute_forum.service;

import nvt.vn.ute_forum.dto.CommentDTO;
import nvt.vn.ute_forum.model.Request;
import jakarta.transaction.Transactional;
import nvt.vn.ute_forum.dto.ForumPostDTO;
import nvt.vn.ute_forum.model.*;
import nvt.vn.ute_forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RequestService {

    @Autowired
    private RequestRepo requestRepo;

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private VoteCommentRepo voteCommentRepo;

    @Autowired
    private VoteRepo voteRepo;
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private DepartmentRepo departmentRepo;

    @Autowired
    private RequestStatusHistoryRepo requestStatushistoryRepo;

    @Autowired
    private ForwardingLogService forwardingLogService;

    @Autowired
    private RequestStatusHistoryService statusHistoryService;

    /**
     * Lấy các bài viết PUBLIC theo trang, kèm reaction, comment count
     * @param pageable phân trang
     * @param currentUserId id user hiện tại
     * @return Page<ForumPostDTO>
     */

    public Page<ForumPostDTO> getPublicPosts(Pageable pageable, String currentUserId) {
        return requestRepo.findByPostStatus("PUBLIC", pageable)
                .map(r -> {
                    ForumPostDTO dto = new ForumPostDTO();
                    dto.setId(r.getId());
                    dto.setSubject(r.getSubject());
                    dto.setDescription(r.getDescription());
                    dto.setDate(r.getTimeCreate());
                    dto.setStatus(r.getCurrentStatus());
                    dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
                    dto.setReactionTypeLower(dto.getReactionType() != null ? dto.getReactionType().toLowerCase() : "");//luu y
                    dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");
                    dto.setAttachments(mapAttachments(r));

                    dto.setCategories(r.getCategories().stream()
                            .map(c -> c.getSubject())
                            .collect(Collectors.toList()));

                    // Count comments
                    long commentCount = commentRepo.countByRequest_Id(r.getId());
                    dto.setCommentCount(commentCount);

                    // Lấy tất cả votes của bài viết
                    List<Vote> votes = voteRepo.findByRequest_Id(r.getId());

                    // Tính số lượng reactions cho 6 loại
                    Map<String, Long> reactionsMap = Map.of(
                            "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                            "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                            "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                            "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                            "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                            "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
                    );
                    dto.setReactions(reactionsMap);

                    // Tổng số reactions
                    long totalReactions = reactionsMap.values().stream().mapToLong(Long::longValue).sum();
                    dto.setTotalReactions(totalReactions);

                    // Reaction của user hiện tại
                    votes.stream()
                            .filter(v -> v.getUser().getId().equals(currentUserId))
                            .findFirst()
                            .ifPresent(v -> dto.setReactionType(v.getType().name()));

                    return dto;
                });
    }

    @Transactional
    public Map<String, Object> votePost(String requestId, String userId, ReactionType type) {
        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vote vote = voteRepo.findById_UserIdAndId_RequestId(userId, requestId).orElse(null);
        String currentType = "";

        if (vote != null) {
            if (vote.getType() == type) {
                voteRepo.delete(vote);
                voteRepo.flush(); // Xóa xong phải flush để tính tổng cho đúng
                currentType = ""; // Trạng thái sau khi Unvote
            } else {
                vote.setType(type);
                vote.setVoteAt(LocalDateTime.now());
                voteRepo.save(vote);
                currentType = type.name();
            }
        } else {
            vote = new Vote(new VoteId(userId, requestId), user, request, type, LocalDateTime.now());
            voteRepo.save(vote);
            currentType = type.name();
        }

        // Lấy tổng số lượng
        List<Vote> votes = voteRepo.findByRequest_Id(requestId);
        Map<String, Long> counts = new HashMap<>();
        for (ReactionType r : ReactionType.values()) counts.put(r.name(), 0L);
        votes.forEach(v -> counts.put(v.getType().name(), counts.get(v.getType().name()) + 1));

        // Trả về cả 2: Số lượng và Trạng thái của user này
        return Map.of("counts", counts, "currentType", currentType);
    }

    public List<ForumPostDTO> getFilteredPosts(String catId, String deptId, String sort, String currentUserId) {
        // Chuyển chuỗi rỗng thành null để câu Query IS NULL ở trên chạy đúng
        String cid = (catId == null || catId.trim().isEmpty()) ? null : catId;
        String did = (deptId == null || deptId.trim().isEmpty()) ? null : deptId;

        // Gọi Repo với các giá trị đã chuẩn hóa
        List<Request> entities = requestRepo.findByFilters(cid, did, sort);

        return entities.stream()
                .map(r -> convertToFullDTO(r, currentUserId))
                .collect(Collectors.toList());
    }

    // --- HÀM TÁI SỬ DỤNG ĐỂ CONVERT DỮ LIỆU ĐẦY ĐỦ ---
    private ForumPostDTO convertToFullDTO(Request r, String currentUserId) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(r.getId());
        dto.setSubject(r.getSubject());
        dto.setDescription(r.getDescription());
        dto.setDate(r.getTimeCreate()); // Dùng timeCreate cho khớp với getPublicPosts
        dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
        dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");
        dto.setAttachments(mapAttachments(r));

        // Categories
        dto.setCategories(r.getCategories().stream()
                .map(Category::getSubject)
                .collect(Collectors.toList()));

        // Count comments
        long commentCount = commentRepo.countByRequest_Id(r.getId());
        dto.setCommentCount(commentCount);

        // Lấy tất cả votes để tính toán Reactions
        List<Vote> votes = voteRepo.findByRequest_Id(r.getId());

        Map<String, Long> reactionsMap = Map.of(
                "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
        );
        dto.setReactions(reactionsMap);

        // Tổng số reactions
        long totalReactions = reactionsMap.values().stream().mapToLong(Long::longValue).sum();
        dto.setTotalReactions(totalReactions);

        // Reaction của user hiện tại (Để tô màu nút Like chẳng hạn)
        if (currentUserId != null) {
            votes.stream()
                    .filter(v -> v.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(v -> {
                        dto.setReactionType(v.getType().name());
                        dto.setReactionTypeLower(v.getType().name().toLowerCase());
                    });
        }

        List<CommentDTO> commentList = commentRepo.findByRequestId(r.getId()).stream()
                .filter(Objects::nonNull)
                .map(c -> {
                    CommentDTO commentDto = new CommentDTO();
                    commentDto.setId(c.getId());
                    commentDto.setContent(c.getContent());
                    commentDto.setUserName(c.getUser() != null ? c.getUser().getFullName() : "Ẩn danh");
                    commentDto.setDate(c.getDate());

                    // --- 1. XỬ LÝ TÍCH XANH (ROLE) ---
                    if (c.getUser() != null && c.getUser().getRole() != null) {
                        // Vì getRole() trả về String (như ADMIN, GV...)
                        commentDto.setUserRole(c.getUser().getRole());
                    } else {
                        commentDto.setUserRole("ROLE_STUDENT");
                    }

                    // --- 2. QUYỀN XÓA ---
                    boolean canDelete = (currentUserId != null && c.getUser() != null && currentUserId.equals(c.getUser().getId()));
                    commentDto.setCanDelete(canDelete);

                    // --- 3. ĐẾM REACTION DỰA TRÊN VOTE_COMMENT ---
                    // Lấy danh sách tất cả vote của comment này từ repository của bà
                    List<VoteComment> vList = voteCommentRepo.findAllByComment_Id(c.getId());

                    Map<String, Long> cmtReactions = new HashMap<>();
                    if (vList != null && !vList.isEmpty()) {
                        // Đếm từng loại dựa trên Enum ReactionType (LIKE, LOVE, HAHA, WOW, SAD, ANGRY)
                        for (ReactionType type : ReactionType.values()) {
                            long count = vList.stream()
                                    .filter(v -> v.getType() == type)
                                    .count();
                            if (count > 0) {
                                cmtReactions.put(type.name(), count);
                            }
                        }

                        // --- 4. CHECK USER HIỆN TẠI ĐÃ THẢ REACTION CHƯA ---
                        if (currentUserId != null) {
                            vList.stream()
                                    .filter(v -> v.getUser().getId().equals(currentUserId))
                                    .findFirst()
                                    .ifPresent(v -> commentDto.setReactionType(v.getType().name()));
                        }
                    }

                    // Nếu không có reaction nào hoặc vList null thì Map sẽ rỗng (JS sẽ tự ẩn count)
                    commentDto.setReactions(cmtReactions);

                    return commentDto;
                })
                .collect(Collectors.toList());

// Cuối cùng gán vào DTO chính
        dto.setComments(commentList);

        return dto;
    }

    private List<ForumPostDTO.AttachmentDTO> mapAttachments(Request request) {
        if (request == null || request.getFileAttachments() == null || request.getFileAttachments().isEmpty()) {
            return Collections.emptyList();
        }

        return request.getFileAttachments().stream()
                .filter(file -> file != null && file.getFileUrl() != null && !file.getFileUrl().isBlank())
                .map(file -> {
                    ForumPostDTO.AttachmentDTO attachmentDTO = new ForumPostDTO.AttachmentDTO();
                    attachmentDTO.setFileName(file.getFileName() == null || file.getFileName().isBlank() ? "Tep dinh kem" : file.getFileName());
                    attachmentDTO.setFileUrl(file.getFileUrl());
                    attachmentDTO.setFileType(file.getFileType() == null ? "unknown" : file.getFileType());
                    return attachmentDTO;
                })
                .collect(Collectors.toList());
    }

    public List<Request> getRequestsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return requestRepo.findByUser_IdOrderByTimeCreateDesc(userId);
    }

    public Optional<Request> getRequestByIdAndUserId(String requestId, String userId) {
        if (requestId == null || requestId.isBlank() || userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return requestRepo.findByIdAndUser_Id(requestId, userId);
    }

    public Request saveOrUpdate(Request request) {
        return requestRepo.save(request);
    }

    @Transactional
    public void deleteRequest(String requestId, String userId) {
        requestRepo.findByIdAndUser_Id(requestId, userId).ifPresent(r -> {
            r.getCategories().clear();
            requestRepo.delete(r);
        });
    }

    public List<ForumPostDTO> searchPosts(String keyword) {
        List<Request> list = requestRepo.searchByKeyword(keyword.trim());

        return list.stream()
                .map(r -> convertToFullDTO(r, null))
                .collect(Collectors.toList());
    }

    public Page<ForumPostDTO> getPublicSearchPosts(String keyword, Pageable pageable, String currentUserId) {
        // 1. Gọi Repo trả về Page<Request>
        Page<Request> requests = requestRepo.searchPublicPosts(keyword.trim(), pageable);

        // 2. Dùng hàm .map() của Page để chuyển đổi từng phần tử
        // Lưu ý: Page.map() trả về một Page mới, không cần dùng stream() hay collect()
        return requests.map(r -> {
            ForumPostDTO dto = new ForumPostDTO();
            dto.setId(r.getId());
            dto.setSubject(r.getSubject());
            dto.setDescription(r.getDescription());
            dto.setDate(r.getTimeCreate());
            dto.setStatus(r.getCurrentStatus());
            dto.setDepartmentName(r.getDepartment() != null ? r.getDepartment().getName() : "N/A");
            dto.setUserName(r.getUser() != null ? r.getUser().getFullName() : "Ẩn danh");
            dto.setAttachments(mapAttachments(r));

            dto.setCategories(r.getCategories().stream()
                    .map(c -> c.getSubject())
                    .collect(Collectors.toList()));

            // Count comments
            dto.setCommentCount(commentRepo.countByRequest_Id(r.getId()));

            // Xử lý Reactions
            List<Vote> votes = voteRepo.findByRequest_Id(r.getId());
            Map<String, Long> reactionsMap = Map.of(
                    "LIKE", votes.stream().filter(v -> v.getType() == ReactionType.LIKE).count(),
                    "LOVE", votes.stream().filter(v -> v.getType() == ReactionType.LOVE).count(),
                    "HAHA", votes.stream().filter(v -> v.getType() == ReactionType.HAHA).count(),
                    "WOW", votes.stream().filter(v -> v.getType() == ReactionType.WOW).count(),
                    "SAD", votes.stream().filter(v -> v.getType() == ReactionType.SAD).count(),
                    "ANGRY", votes.stream().filter(v -> v.getType() == ReactionType.ANGRY).count()
            );
            dto.setReactions(reactionsMap);
            dto.setTotalReactions(reactionsMap.values().stream().mapToLong(Long::longValue).sum());

            // Reaction của user hiện tại
            if (currentUserId != null) {
                votes.stream()
                        .filter(v -> v.getUser().getId().equals(currentUserId))
                        .findFirst()
                        .ifPresent(v -> {
                            dto.setReactionType(v.getType().name());
                            dto.setReactionTypeLower(v.getType().name().toLowerCase());
                        });
            }

            return dto;
        });
    }

    public Page<Request> getAllFeedbacks(Pageable pageable, Users user) {

        String role = user.getRole();

        if (role.equals("ROLE_ADMIN")) {
            return requestRepo.findAll(pageable);
        }

        if (role.equals("ROLE_DEPARTMENT")) {
            return requestRepo.findByDepartment_Id(
                    user.getDepartment().getId(),
                    pageable
            );
        }

        // 🔥 fallback (bắt buộc phải có)
        return Page.empty();
    }

    @Transactional
    public Request getFeedbackDetail(String id, Users currentUser) {

        Request request = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        String role = currentUser.getRole();

        // 👨‍🎓 STUDENT
        if (role.equals("ROLE_STUDENT") &&
                !request.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Không có quyền xem");
        }

        // 👨‍💼 DEPARTMENT (STAFF)
        if (role.equals("ROLE_DEPARTMENT") &&
                !request.getDepartment().getId()
                        .equals(currentUser.getDepartment().getId())) {
            throw new RuntimeException("Không cùng phòng ban");
        }

        // 👑 ADMIN → xem tất cả

        // load lazy
        request.getUser().getFullName();
        request.getCategories().size();
        request.getComments().size();
        request.getFileAttachments().size();

        return request;
    }

    public Page<Request> searchFeedbacks(String keyword, Pageable pageable, Users user) {

        String role = user.getRole();

        // ADMIN → search tất cả
        if (role.equals("ROLE_ADMIN")) {
            return requestRepo.findByContentContaining(keyword, pageable);
        }

        //  DEPARTMENT → chỉ search trong phòng ban
        if (role.equals("ROLE_DEPARTMENT")) {
            return requestRepo.findByContentContainingAndDepartment_Id(
                    keyword,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }

    // Thêm vào sau hàm getPublicPosts hoặc cuối file đều được bà nhé
    public ForumPostDTO getPostDetail(String postId, String currentUserId) {
        return requestRepo.findById(postId)
                .map(r -> convertToFullDTO(r, currentUserId)) // Tận dụng hàm xịn bà đã có ở dòng 122
                .orElse(null);
    }

    public Request getRequestById(String requestId) {
        return requestRepo.findById(requestId).orElse(null);
    }

    public Page<Request> filterFeedbacks(String categoryId, Pageable pageable, Users user) {

        if (user.getRole().equals("ROLE_ADMIN")) {
            return requestRepo.findByCategory(categoryId, pageable);
        }

        if (user.getRole().equals("ROLE_DEPARTMENT")) {
            return requestRepo.findByCategoryAndDepartment(
                    categoryId,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }

    public Page<Request> filterByStatus(String status, Pageable pageable, Users user) {

        if ("ALL".equals(status)) {
            return getAllFeedbacks(pageable, user);
        }

        if (user.getRole().equals("ROLE_ADMIN")) {
            return requestRepo.findByCurrentStatus(status, pageable);
        }

        if (user.getRole().equals("ROLE_DEPARTMENT")) {
            return requestRepo.findByCurrentStatusAndDepartment_Id(
                    status,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }
    public Page<Request> getFeedbacks(
            String category,
            String status,
            Pageable pageable,
            Users user) {

        // 🔥 normalize dữ liệu
        if ("ALL".equals(category)) category = null;
        if ("ALL".equals(status)) status = null;

        if (category == null && status == null) {
            return getAllFeedbacks(pageable, user);
        }

        if (category != null && status == null) {
            return filterFeedbacks(category, pageable, user);
        }

        if (category == null && status != null) {
            return filterByStatus(status, pageable, user);
        }

        // 👉 BOTH
        return filterByCategoryAndStatus(category, status, pageable, user);
    }
    public Page<Request> filterByCategoryAndStatus(
            String categoryId,
            String status,
            Pageable pageable,
            Users user) {

        if (user.getRole().equals("ROLE_ADMIN")) {
            return requestRepo.findByCategoryAndStatus(categoryId, status, pageable);
        }

        if (user.getRole().equals("ROLE_DEPARTMENT")) {
            return requestRepo.findByCategoryStatusAndDepartment(
                    categoryId,
                    status,
                    user.getDepartment().getId(),
                    pageable
            );
        }

        return Page.empty();
    }

    public List<String> getAvailableStatuses(Users user) {

        if (user.getRole().equals("ROLE_ADMIN")) {
            return List.of("PENDING", "APPROVED", "RESOLVED", "FORWARDING", "REJECTED");
        }

        if (user.getRole().equals("ROLE_DEPARTMENT")) {
            return List.of("PENDING", "APPROVED", "RESOLVED", "FORWARDING");
        }

        return List.of();
    }



    @Transactional
    public Request getAdminFeedbackDetail(String id) {
        Request request = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy góp ý: " + id));

        request.getUser().getFullName();
        request.getCategories().size();
        request.getComments().size();
        request.getFileAttachments().size();
        request.getForwardingLogs().size();

        return request;
    }

    /**
     * Tìm kiếm tất cả feedback - admin không lọc phòng ban
     */
    public Page<Request> searchAllFeedbacks(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return requestRepo.findAll(pageable);
        }
        return requestRepo.findAllByContentContaining(keyword.trim(), pageable);
    }

    /**
     * Lọc feedback theo departmentId + categoryId + status - dành cho admin
     * Không giới hạn phòng ban, lấy tất cả request kể cả ẩn danh
     */
    public Page<Request> getAdminFeedbacks(
            String departmentId,
            String categoryId,
            String status,
            Pageable pageable) {

        // Nếu không có filter nào -> lấy tất cả
        if (departmentId == null && categoryId == null && status == null) {
            return requestRepo.findAll(pageable);
        }

        // Có ít nhất 1 filter -> dùng query tổng hợp
        return requestRepo.adminFilterAll(departmentId, categoryId, status, pageable);
    }

    @Transactional
    public void updateStatus(String requestId, String newStatus) {

        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        String current = request.getCurrentStatus();

        // 🚫 RULE: Không cho update nếu đã kết thúc
        if (current.equals("RESOLVED") || current.equals("REJECTED")) {
            throw new RuntimeException("Không thể cập nhật trạng thái này nữa!");
        }

        // ✅ RULE chuyển trạng thái hợp lệ
        boolean valid = switch (current) {
            case "PENDING" ->
                    newStatus.equals("APPROVED") ||
                            newStatus.equals("RESOLVED") ||
                            newStatus.equals("REJECTED");
            case "APPROVED" -> newStatus.equals("RESOLVED") || newStatus.equals("REJECTED") || newStatus.equals("FORWARDING");
            default -> false;
        };

        if (!valid) {
            throw new RuntimeException("Chuyển trạng thái không hợp lệ!");
        }

        // 🔥 1. update current status
        request.setCurrentStatus(newStatus);
        requestRepo.save(request);

        // 🔥 2. lưu history
        RequestStatusHistory history = new RequestStatusHistory();
        history.setId("RSH_" + System.nanoTime());
        history.setStatus(newStatus);
        history.setCreateAt(LocalDateTime.now());
        history.setRequest(request);

        requestStatushistoryRepo.save(history);
    }

    @Transactional
    public void forwardRequest(String requestId,
                               String toDeptId,
                               String note,
                               Users user) {

        Request request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Department fromDept = request.getDepartment();

        Department toDept = departmentRepo.findById(toDeptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        if (fromDept.getId().equals(toDeptId)) {
            throw new RuntimeException("Không thể chuyển cùng phòng");
        }

        request.setDepartment(toDept);
        request.setCurrentStatus("PENDING");

        requestRepo.save(request);

        forwardingLogService.createLog(request, fromDept, toDept, note, user);

        statusHistoryService.createForwardStatus(request);
    }


}


