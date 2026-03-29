-- DROP DATABASE ute_forum;
-- CREATE DATABASE ute_forum;

USE ute_forum;

START TRANSACTION;

-- 1) Bổ sung thêm Department (Phòng Công tác sinh viên, Thư viện, Tài chính)
INSERT IGNORE INTO `department` (`id`, `name`, `description`, `email`, `phone`, `location`, `isactive`) VALUES
('DEP_DT', 'Phong Dao tao', 'Quan ly chuong trinh hoc', 'daotao@ute.edu.vn', '0901000006', 'Toa A', TRUE),
('DEP_TCCB', 'Phong To chuc Can bo', 'Quan ly nhan su', 'tccb@ute.edu.vn', '0901000007', 'Toa A', TRUE),
('DEP_KHCN', 'Phong Khoa hoc Cong nghe', 'Quan ly nghien cuu', 'khcn@ute.edu.vn', '0901000008', 'Toa C', TRUE),
('DEP_QTBT', 'Phong Quan tri Thiet bi', 'Bao tri co so vat chat', 'qtbt@ute.edu.vn', '0901000009', 'Toa B', TRUE),
('DEP_QHQT', 'Phong Quan he Quoc te', 'Hop tac quoc te', 'iro@ute.edu.vn', '0901000010', 'Toa A', TRUE),
('DEP_TTTS', 'Trung tam Tuyen sinh', 'Tu van tuyen sinh', 'tuyensinh@ute.edu.vn', '0901000011', 'Toa E', TRUE),
('DEP_KDBL', 'Khoa Dien - Dien tu', 'Quan ly nganh Dien', 'feee@ute.edu.vn', '0901000012', 'Toa D', TRUE),
('DEP_KCK', 'Khoa Co khi', 'Quan ly nganh Co khi', 'fme@ute.edu.vn', '0901000013', 'Toa B', TRUE),
('DEP_KCNTT', 'Khoa Cong nghe Thong tin', 'Quan ly nganh CNTT', 'fit@ute.edu.vn', '0901000014', 'Toa E', TRUE),
('DEP_YTE', 'Tram Y te', 'Cham soc suc khoe', 'yte@ute.edu.vn', '0901000015', 'Toa A', TRUE),
('DEP_CTSV', 'Phong CTSV', 'Ho tro sinh vien', 'ctsv@ute.edu.vn', '0901000003', 'Toa C', TRUE),
('DEP_TV', 'Thu vien', 'Quan ly sach', 'lib@ute.edu.vn', '0901000004', 'Toa E', TRUE),
('DEP_KHTC', 'Phong Ke hoach Tai chinh', 'Hoc phi', 'khtc@ute.edu.vn', '0901000005', 'Toa A', TRUE);

INSERT INTO `users` (`id`, `fullname`, `email`, `password`, `role`, `department_id`) VALUES
-- 5 CHUYÊN VIÊN PHÒNG BAN (ROLE_DEPARTMENT)
('U_DEP_05', 'Le Van Dao Tao', 'dep05@ute.edu.vn', 'dept_pass_05', 'ROLE_DEPARTMENT', 'DEP_DT'),
('U_DEP_06', 'Phan Thi Tuyen Sinh', 'dep06@ute.edu.vn', 'dept_pass_06', 'ROLE_DEPARTMENT', 'DEP_TTTS'),
('U_DEP_07', 'Ngo Van Ky Thuat', 'dep07@ute.edu.vn', 'dept_pass_07', 'ROLE_DEPARTMENT', 'DEP_KCNTT'),
('U_DEP_08', 'Trinh Van Co Khi', 'dep08@ute.edu.vn', 'dept_pass_08', 'ROLE_DEPARTMENT', 'DEP_KCK'),
('U_DEP_09', 'Vu Thi Y Te', 'dep09@ute.edu.vn', 'dept_pass_09', 'ROLE_DEPARTMENT', 'DEP_YTE'),

-- 45 SINH VIÊN (ROLE_STUDENT)
('U_STU_10', 'Tran Thanh Tam', 'tam10@ute.edu.vn', 'pass_tam_10', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_11', 'Le Minh Chuc', 'chuc11@ute.edu.vn', 'pass_chuc_11', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_12', 'Hoang Anh Tuan', 'tuan12@ute.edu.vn', 'pass_tuan_12', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_13', 'Pham Bao Thy', 'thy13@ute.edu.vn', 'pass_thy_13', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_14', 'Vo Van Quyet', 'quyet14@ute.edu.vn', 'pass_quyet_14', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_15', 'Nguyen Thi Mai', 'mai15@ute.edu.vn', 'pass_mai_15', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_16', 'Bui Duc Phuc', 'phuc16@ute.edu.vn', 'pass_phuc_16', 'ROLE_STUDENT', 'DEP_DT'),
('U_STU_17', 'Dang Hong Ngoc', 'ngoc17@ute.edu.vn', 'pass_ngoc_17', 'ROLE_STUDENT', 'DEP_CTSV'),
('U_STU_18', 'Truong Gia Bao', 'bao18@ute.edu.vn', 'pass_bao_18', 'ROLE_STUDENT', 'DEP_TV'),
('U_STU_19', 'Diep Lien Hoa', 'hoa19@ute.edu.vn', 'pass_hoa_19', 'ROLE_STUDENT', 'DEP_KHTC'),
('U_STU_20', 'Ly Thanh Long', 'long20@ute.edu.vn', 'pass_long_20', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_21', 'Dinh Cong Manh', 'manh21@ute.edu.vn', 'pass_manh_21', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_22', 'Lam Khanh Chi', 'chi22@ute.edu.vn', 'pass_chi_22', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_23', 'Quach Tuan Du', 'du23@ute.edu.vn', 'pass_du_23', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_24', 'Doan Ngoc Hai', 'hai24@ute.edu.vn', 'pass_hai_24', 'ROLE_STUDENT', 'DEP_TTTS'),
('U_STU_25', 'Kieu Thi Diem', 'diem25@ute.edu.vn', 'pass_diem_25', 'ROLE_STUDENT', 'DEP_YTE'),
('U_STU_26', 'Phung Khac Khoan', 'khoan26@ute.edu.vn', 'pass_khoan_26', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_27', 'Cao Thai Son', 'son27@ute.edu.vn', 'pass_son_27', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_28', 'Vu Cat Tuong', 'tuong28@ute.edu.vn', 'pass_tuong_28', 'ROLE_STUDENT', 'DEP_CTSV'),
('U_STU_29', 'Hua Vi Van', 'van29@ute.edu.vn', 'pass_van_29', 'ROLE_STUDENT', 'DEP_DT'),
('U_STU_30', 'Ta Quang Buu', 'buu30@ute.edu.vn', 'pass_buu_30', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_31', 'Ton Duc Thang', 'thang31@ute.edu.vn', 'pass_thang_31', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_32', 'Luong The Vinh', 'vinh32@ute.edu.vn', 'pass_vinh_32', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_33', 'Chu Van An', 'an33@ute.edu.vn', 'pass_an_33', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_34', 'Mac Dinh Chi', 'chi34@ute.edu.vn', 'pass_chi_34', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_35', 'Ngo Bao Chau', 'chau35@ute.edu.vn', 'pass_chau_35', 'ROLE_STUDENT', 'DEP_DT'),
('U_STU_36', 'Tran Dai Nghia', 'nghia36@ute.edu.vn', 'pass_nghia_36', 'ROLE_STUDENT', 'DEP_KHCN'),
('U_STU_37', 'Phan Boi Chau', 'chau37@ute.edu.vn', 'pass_chau_37', 'ROLE_STUDENT', 'DEP_CTSV'),
('U_STU_38', 'Huynh Thuc Khang', 'khang38@ute.edu.vn', 'pass_khang_38', 'ROLE_STUDENT', 'DEP_TV'),
('U_STU_39', 'Nguyen Van Vinh', 'vinh39@ute.edu.vn', 'pass_vinh_39', 'ROLE_STUDENT', 'DEP_KHTC'),
('U_STU_40', 'Bach Thai Buoi', 'buoi40@ute.edu.vn', 'pass_buoi_40', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_41', 'Trinh Cong Son', 'son41@ute.edu.vn', 'pass_son_41', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_42', 'Xuan Quynh', 'quynh42@ute.edu.vn', 'pass_quynh_42', 'ROLE_STUDENT', 'DEP_QHQT'),
('U_STU_43', 'Nam Cao', 'cao43@ute.edu.vn', 'pass_cao_43', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_44', 'To Huu', 'huu44@ute.edu.vn', 'pass_huu_44', 'ROLE_STUDENT', 'DEP_DT'),
('U_STU_45', 'Vo Nguyen Giap', 'giap45@ute.edu.vn', 'pass_giap_45', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_46', 'Nguyen Trai', 'trai46@ute.edu.vn', 'pass_trai_46', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_47', 'Le Loi', 'loi47@ute.edu.vn', 'pass_loi_47', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_48', 'Quang Trung', 'trung48@ute.edu.vn', 'pass_trung_48', 'ROLE_STUDENT', 'DEP_KHCN'),
('U_STU_49', 'An Duong Vuong', 'vuong49@ute.edu.vn', 'pass_vuong_49', 'ROLE_STUDENT', 'DEP_QHQT'),
('U_STU_50', 'Au Co', 'co50@ute.edu.vn', 'pass_co_50', 'ROLE_STUDENT', 'DEP_YTE'),
('U_STU_51', 'Lac Long Quan', 'quan51@ute.edu.vn', 'pass_quan_51', 'ROLE_STUDENT', 'DEP_KCNTT'),
('U_STU_52', 'Thanh Giong', 'giong52@ute.edu.vn', 'pass_giong_52', 'ROLE_STUDENT', 'DEP_KCK'),
('U_STU_53', 'Son Tinh', 'tinh53@ute.edu.vn', 'pass_tinh_53', 'ROLE_STUDENT', 'DEP_KDBL'),
('U_STU_54', 'Thuy Tinh', 'tinh54@ute.edu.vn', 'pass_tinh_54', 'ROLE_STUDENT', 'DEP_TTTS');

-- 3) Bổ sung Category (Học bổng, Học phí, Cơ sở vật chất)
INSERT INTO `categorie` (`id`, `subject`, `isactive`) VALUES
('CAT_HOCBONG', 'Hoc bong & Tro cap', TRUE),
('CAT_HOCPHI', 'Hoc phi & Hoa don', TRUE),
('CAT_CSVC', 'Co so vat chat & Thiet bi', TRUE),
('CAT_KHAC', 'Cac van de khac', TRUE);
INSERT INTO `categorie` (`id`, `subject`, `isactive`) VALUES
('CAT_HOCVU', 'Học vụ & Đào tạo', TRUE),
('CAT_KYTHUAT', 'Hỗ trợ Kỹ thuật', TRUE),
('CAT_TAICHINH', 'Học phí & Học bổng', TRUE);

-- 4) Bổ sung Request (Đa dạng trạng thái: RESOLVED, REJECTED, PENDING)
INSERT INTO `request` (`id`, `subject`, `description`, `currentstatus`, `timecreate`, `poststatus`, `department_id`, `user_id`) VALUES
-- NHÓM KHOA CNTT (DEP_KCNTT)
('REQ_008', 'Lỗi đăng nhập Portal', 'Em không thể đăng nhập bằng mail sinh viên', 'RESOLVED', '2026-03-24', 'PUBLIC', 'DEP_KCNTT', 'U_STU_10'),
('REQ_009', 'Xin cấp lại mật khẩu Wifi', 'Mật khẩu Wifi tòa E thay đổi em không vào được', 'PENDING', '2026-03-24', 'PRIVATE', 'DEP_KCNTT', 'U_STU_11'),
('REQ_010', 'Hỏi về chuẩn đầu ra IT', 'Chứng chỉ quốc tế nào được chấp nhận thay thế?', 'FORWARDING', '2026-03-25', 'PUBLIC', 'DEP_KCNTT', 'U_STU_20'),
('REQ_011', 'Lỗi phần mềm thực hành', 'Máy tính phòng Lab 302 không chạy được Docker', 'RESOLVED', '2026-03-25', 'PUBLIC', 'DEP_KCNTT', 'U_STU_21'),
('REQ_012', 'Mượn phòng Lab làm đồ án', 'Nhóm em muốn đăng ký trực phòng tối thứ 7', 'REJECTED', '2026-03-26', 'PUBLIC', 'DEP_KCNTT', 'U_STU_26'),

-- NHÓM PHÒNG ĐÀO TẠO (DEP_DT)
('REQ_013', 'Đính chính thông tin cá nhân', 'Hệ thống sai ngày sinh của em trên danh sách thi', 'RESOLVED', '2026-03-24', 'PRIVATE', 'DEP_DT', 'U_STU_16'),
('REQ_014', 'Phúc khảo điểm thi', 'Em muốn phúc khảo môn Toán cao cấp 2', 'PENDING', '2026-03-25', 'PUBLIC', 'DEP_DT', 'U_STU_29'),
('REQ_015', 'Trùng lịch thi học kỳ', 'Hai môn chuyên ngành của em bị thi cùng giờ', 'FORWARDING', '2026-03-26', 'PUBLIC', 'DEP_DT', 'U_STU_35'),
('REQ_016', 'Hỏi về điều kiện tốt nghiệp', 'Em thiếu 2 tín chỉ tự chọn có được xét không?', 'RESOLVED', '2026-03-27', 'PUBLIC', 'DEP_DT', 'U_STU_44'),
('REQ_017', 'Đăng ký học cải thiện', 'Lớp học phần em muốn đăng ký đã bị đầy', 'REJECTED', '2026-03-27', 'PUBLIC', 'DEP_DT', 'U_STU_52'),

-- NHÓM KHOA ĐIỆN (DEP_KDBL)
('REQ_018', 'Mượn linh kiện thực hành', 'Em cần mượn bộ kit Arduino cho bài tập lớn', 'RESOLVED', '2026-03-25', 'PUBLIC', 'DEP_KDBL', 'U_STU_12'),
('REQ_019', 'Lịch bảo trì xưởng điện', 'Khi nào xưởng mở cửa lại để tụi em làm mô hình?', 'PENDING', '2026-03-25', 'PUBLIC', 'DEP_KDBL', 'U_STU_13'),
('REQ_020', 'Hỏi về đồ án chuyên ngành', 'Cách thức chọn giảng viên hướng dẫn đợt này', 'FORWARDING', '2026-03-26', 'PUBLIC', 'DEP_KDBL', 'U_STU_22'),
('REQ_021', 'Xin đổi ca thực hành', 'Em bị kẹt lịch làm thêm vào sáng thứ 4', 'REJECTED', '2026-03-27', 'PRIVATE', 'DEP_KDBL', 'U_STU_27'),
('REQ_022', 'Lỗi thiết bị phòng máy', 'Máy hiện sóng số 05 bị hỏng màn hình', 'RESOLVED', '2026-03-28', 'PUBLIC', 'DEP_KDBL', 'U_STU_32'),

-- NHÓM KHOA CƠ KHÍ (DEP_KCK)
('REQ_023', 'Đăng ký xưởng hàn', 'Em đăng ký ca chiều nhưng hệ thống báo lỗi', 'RESOLVED', '2026-03-26', 'PUBLIC', 'DEP_KCK', 'U_STU_14'),
('REQ_024', 'Hỏi về thực tập doanh nghiệp', 'Công ty ngoài danh sách có được chấp nhận không?', 'PENDING', '2026-03-26', 'PUBLIC', 'DEP_KCK', 'U_STU_15'),
('REQ_025', 'Xin cấp bù phôi thép', 'Nhóm em làm hỏng phôi cần xin lại để nộp bài', 'REJECTED', '2026-03-27', 'PRIVATE', 'DEP_KCK', 'U_STU_23'),
('REQ_026', 'Tư vấn hướng nghiệp', 'Cơ hội việc làm ngành Cơ điện tử hiện nay', 'RESOLVED', '2026-03-28', 'PUBLIC', 'DEP_KCK', 'U_STU_31'),
('REQ_027', 'Lỗi hiển thị điểm thực hành', 'Giảng viên đã chấm nhưng hệ thống vẫn để 0', 'FORWARDING', '2026-03-29', 'PUBLIC', 'DEP_KCK', 'U_STU_34'),

-- NHÓM CTSV (DEP_CTSV)
('REQ_028', 'Mất thẻ sinh viên', 'Em làm rơi thẻ tại sảnh tòa C sáng nay', 'PENDING', '2026-03-25', 'PUBLIC', 'DEP_CTSV', 'U_STU_17'),
('REQ_029', 'Hỏi về điểm rèn luyện', 'Em tham gia hiến máu nhưng chưa thấy cập nhật', 'RESOLVED', '2026-03-26', 'PUBLIC', 'DEP_CTSV', 'U_STU_28'),
('REQ_030', 'Đăng ký vay vốn sinh viên', 'Thủ tục xác nhận cho ngân hàng chính sách', 'FORWARDING', '2026-03-27', 'PRIVATE', 'DEP_CTSV', 'U_STU_37'),

-- NHÓM THƯ VIỆN (DEP_TV)
('REQ_031', 'Sách bị quá hạn do ốm', 'Em nằm viện nên không đi trả đúng hạn được', 'RESOLVED', '2026-03-27', 'PRIVATE', 'DEP_TV', 'U_STU_18'),
('REQ_032', 'Đề xuất mua giáo trình mới', 'Thư viện chưa có bộ sách Java Spring Boot mới nhất', 'PENDING', '2026-03-28', 'PUBLIC', 'DEP_TV', 'U_STU_38'),

-- NHÓM TÀI CHÍNH (DEP_KHTC)
('REQ_033', 'Hoàn phí bảo hiểm', 'Em đã đóng trùng 2 lần phí bảo hiểm y tế', 'RESOLVED', '2026-03-29', 'PRIVATE', 'DEP_KHTC', 'U_STU_19'),
('REQ_034', 'Gia hạn nộp học phí', 'Gia đình em gặp khó khăn xin nộp trễ 2 tuần', 'REJECTED', '2026-03-30', 'PRIVATE', 'DEP_KHTC', 'U_STU_39'),

-- NHÓM Y TẾ (DEP_YTE)
('REQ_035', 'Khám sức khỏe bổ sung', 'Em bỏ lỡ buổi khám tập trung do bị kẹt xe', 'PENDING', '2026-03-25', 'PUBLIC', 'DEP_YTE', 'U_STU_25'),
('REQ_036', 'Hỏi về thẻ BHYT mới', 'Khi nào thì lớp em được phát thẻ giấy ạ?', 'RESOLVED', '2026-03-31', 'PUBLIC', 'DEP_YTE', 'U_STU_50'),

-- NHÓM TUYỂN SINH (DEP_TTTS)
('REQ_037', 'Xin rút hồ sơ chuyển trường', 'Em muốn chuyển sang trường khác gần nhà hơn', 'FORWARDING', '2026-03-26', 'PRIVATE', 'DEP_TTTS', 'U_STU_24'),
('REQ_038', 'Hỏi về học bổng đầu vào', 'Em thuộc diện tuyển thẳng có được ưu đãi gì không?', 'RESOLVED', '2026-04-01', 'PUBLIC', 'DEP_TTTS', 'U_STU_54'),

-- DỮ LIỆU TRỘN THÊM (Đủ 50 dòng)
('REQ_039', 'Lỗi Wi-Fi tầng 5 tòa A', 'Kết nối rất yếu không thể tra cứu tài liệu', 'PENDING', '2026-03-30', 'PUBLIC', 'DEP_KCNTT', 'U_STU_30'),
('REQ_040', 'Đăng ký đề tài NCKH', 'Nhóm em muốn đăng ký đề tài về xe tự hành', 'RESOLVED', '2026-03-31', 'PUBLIC', 'DEP_KHCN', 'U_STU_36'),
('REQ_041', 'Hỏi về visa trao đổi', 'Thủ tục đi trao đổi tại Hàn Quốc đợt tới', 'FORWARDING', '2026-04-01', 'PRIVATE', 'DEP_QHQT', 'U_STU_42'),
('REQ_042', 'Sửa máy chiếu phòng C103', 'Máy chiếu bị sọc vàng không xem được hình', 'RESOLVED', '2026-04-02', 'PUBLIC', 'DEP_QTBT', 'U_STU_45'),
('REQ_043', 'Báo mất ví tại canteen', 'Trong ví có CCCD tên Nguyễn Văn A', 'PENDING', '2026-04-03', 'PUBLIC', 'DEP_CTSV', 'U_STU_47'),
('REQ_044', 'Hỏi về thủ tục tốt nghiệp sớm', 'Em đã tích lũy đủ 140 tín chỉ', 'RESOLVED', '2026-04-04', 'PUBLIC', 'DEP_DT', 'U_STU_35'),
('REQ_045', 'Cấp giấy xác nhận sinh viên', 'Cần giấy để bổ sung hồ sơ xin việc bán thời gian', 'RESOLVED', '2026-04-04', 'PRIVATE', 'DEP_CTSV', 'U_STU_54'),
('REQ_046', 'Hỏi lịch thi khảo sát Anh văn', 'Lịch thi dự kiến vào tháng mấy ạ?', 'PENDING', '2026-04-05', 'PUBLIC', 'DEP_TTTS', 'U_STU_54'),
('REQ_047', 'Cập nhật điểm rèn luyện HK1', 'Em bị thiếu điểm hoạt động phong trào tháng 12', 'FORWARDING', '2026-04-05', 'PUBLIC', 'DEP_CTSV', 'U_STU_28'),
('REQ_048', 'Lỗi nộp học phí qua ví điện tử', 'Tiền đã trừ nhưng hệ thống chưa báo thành công', 'PENDING', '2026-04-06', 'PRIVATE', 'DEP_KHTC', 'U_STU_19'),
('REQ_049', 'Mượn hội trường tổ chức CLB', 'CLB Tiếng Anh muốn mượn sảnh tòa E tối thứ 6', 'REJECTED', '2026-04-06', 'PUBLIC', 'DEP_QTBT', 'U_STU_51'),
('REQ_050', 'Hỏi về chứng chỉ MOS', 'Nộp chứng chỉ này có được miễn học tin học đại cương?', 'RESOLVED', '2026-04-07', 'PUBLIC', 'DEP_KCNTT', 'U_STU_10'),
('REQ_051', 'Xin cấp lại bảng điểm chính thức', 'Em cần bảng điểm tiếng Anh để xin học bổng', 'PENDING', '2026-04-07', 'PUBLIC', 'DEP_DT', 'U_STU_16'),
('REQ_052', 'Gia hạn sách mượn online', 'Trang web báo lỗi không cho gia hạn sách', 'RESOLVED', '2026-04-08', 'PUBLIC', 'DEP_TV', 'U_STU_18'),
('REQ_053', 'Hỏi về kỳ hè năm nay', 'Năm nay có mở lớp hè môn Triết học không ạ?', 'PENDING', '2026-04-08', 'PUBLIC', 'DEP_DT', 'U_STU_52'),
('REQ_054', 'Lỗi hiển thị GPA', 'GPA học kỳ trước của em bị tính sai 0.1 điểm', 'FORWARDING', '2026-04-09', 'PRIVATE', 'DEP_DT', 'U_STU_41'),
('REQ_055', 'Xin giấy giới thiệu thực tập', 'Em xin thực tập tại công ty Intel', 'RESOLVED', '2026-04-09', 'PUBLIC', 'DEP_KCNTT', 'U_STU_45'),
('REQ_056', 'Hỏi về chuẩn ngoại ngữ K23', 'Yêu cầu TOEIC đợt này là bao nhiêu?', 'PENDING', '2026-04-10', 'PUBLIC', 'DEP_TTTS', 'U_STU_54'),
('REQ_057', 'Cấp lại mã số sinh viên', 'Em bị mất mật khẩu Mail và không nhớ MSSV', 'RESOLVED', '2026-04-10', 'PRIVATE', 'DEP_DT', 'U_STU_53');
-- 5) Map Category <-> Request
INSERT INTO `categorycontainrequest` (`category_id`, `request_id`) VALUES
-- Nhóm Học vụ (Đào tạo, điểm số, tốt nghiệp)
('CAT_HOCVU', 'REQ_013'), ('CAT_HOCVU', 'REQ_014'), ('CAT_HOCVU', 'REQ_015'), 
('CAT_HOCVU', 'REQ_016'), ('CAT_HOCVU', 'REQ_017'), ('CAT_HOCVU', 'REQ_020'), 
('CAT_HOCVU', 'REQ_027'), ('CAT_HOCVU', 'REQ_037'), ('CAT_HOCVU', 'REQ_044'), 
('CAT_HOCVU', 'REQ_046'), ('CAT_HOCVU', 'REQ_050'), ('CAT_HOCVU', 'REQ_051'), 
('CAT_HOCVU', 'REQ_053'), ('CAT_HOCVU', 'REQ_054'), ('CAT_HOCVU', 'REQ_056'), 
('CAT_HOCVU', 'REQ_057'),

-- Nhóm Kỹ thuật & CNTT (Portal, Wifi, Phần mềm)
('CAT_KYTHUAT', 'REQ_008'), ('CAT_KYTHUAT', 'REQ_009'), ('CAT_KYTHUAT', 'REQ_010'), 
('CAT_KYTHUAT', 'REQ_011'), ('CAT_KYTHUAT', 'REQ_034'), ('CAT_KYTHUAT', 'REQ_039'), 
('CAT_KYTHUAT', 'REQ_055'),

-- Nhóm Cơ sở vật chất & Thư viện (Phòng Lab, Sách, Thiết bị)
('CAT_CSVC', 'REQ_012'), ('CAT_CSVC', 'REQ_018'), ('CAT_CSVC', 'REQ_019'), 
('CAT_CSVC', 'REQ_022'), ('CAT_CSVC', 'REQ_023'), ('CAT_CSVC', 'REQ_025'), 
('CAT_CSVC', 'REQ_031'), ('CAT_CSVC', 'REQ_032'), ('CAT_CSVC', 'REQ_042'), 
('CAT_CSVC', 'REQ_049'), ('CAT_CSVC', 'REQ_052'),

-- Nhóm Tài chính & Học bổng (Học phí, BHYT, Vay vốn)
('CAT_TAICHINH', 'REQ_028'), ('CAT_TAICHINH', 'REQ_030'), ('CAT_TAICHINH', 'REQ_033'), 
('CAT_TAICHINH', 'REQ_034'), ('CAT_TAICHINH', 'REQ_036'), ('CAT_TAICHINH', 'REQ_038'), 
('CAT_TAICHINH', 'REQ_048'),

-- Nhóm Khác (Tư vấn, NCKH, Đời sống)
('CAT_KHAC', 'REQ_021'), ('CAT_KHAC', 'REQ_024'), ('CAT_KHAC', 'REQ_026'), 
('CAT_KHAC', 'REQ_029'), ('CAT_KHAC', 'REQ_035'), ('CAT_KHAC', 'REQ_040'), 
('CAT_KHAC', 'REQ_041'), ('CAT_KHAC', 'REQ_043'), ('CAT_KHAC', 'REQ_045'), 
('CAT_KHAC', 'REQ_047');

-- -- 7) Message (Hội thoại giải đáp cho REQ_004)
-- INSERT INTO `clarificationconversation` (`id`, `isopen`, `createat`, `request_id`) VALUES
-- -- Nhóm kết thúc (isopen = FALSE)
-- ('CC_008', FALSE, '2026-03-24', 'REQ_008'),
-- ('CC_011', FALSE, '2026-03-26', 'REQ_011'),
-- ('CC_012', FALSE, '2026-03-27', 'REQ_012'),
-- ('CC_013', FALSE, '2026-03-24', 'REQ_013'),
-- ('CC_016', FALSE, '2026-03-27', 'REQ_016'),
-- ('CC_017', FALSE, '2026-03-28', 'REQ_017'),
-- ('CC_018', FALSE, '2026-03-25', 'REQ_018'),
-- ('CC_021', FALSE, '2026-03-28', 'REQ_021'),
-- ('CC_022', FALSE, '2026-03-29', 'REQ_022'),
-- ('CC_023', FALSE, '2026-03-27', 'REQ_023'),
-- ('CC_025', FALSE, '2026-03-28', 'REQ_025'),
-- ('CC_026', FALSE, '2026-03-29', 'REQ_026'),
-- ('CC_029', FALSE, '2026-03-27', 'REQ_029'),
-- ('CC_031', FALSE, '2026-03-28', 'REQ_031'),
-- ('CC_033', FALSE, '2026-03-30', 'REQ_033'),
-- ('CC_034', FALSE, '2026-03-31', 'REQ_034'),
-- ('CC_036', FALSE, '2026-04-01', 'REQ_036'),
-- ('CC_038', FALSE, '2026-04-02', 'REQ_038'),
-- ('CC_040', FALSE, '2026-04-01', 'REQ_040'),
-- ('CC_042', FALSE, '2026-04-03', 'REQ_042'),
-- ('CC_044', FALSE, '2026-04-05', 'REQ_044'),
-- ('CC_045', FALSE, '2026-04-05', 'REQ_045'),
-- ('CC_049', FALSE, '2026-04-07', 'REQ_049'),
-- ('CC_050', FALSE, '2026-04-08', 'REQ_050'),
-- ('CC_052', FALSE, '2026-04-09', 'REQ_052'),
-- ('CC_055', FALSE, '2026-04-10', 'REQ_055'),
-- ('CC_057', FALSE, '2026-04-11', 'REQ_057'),

-- -- Nhóm đang mở (isopen = TRUE)
-- ('CC_009', TRUE, '2026-03-24', 'REQ_009'),
-- ('CC_010', TRUE, '2026-03-25', 'REQ_010'),
-- ('CC_014', TRUE, '2026-03-25', 'REQ_014'),
-- ('CC_015', TRUE, '2026-03-26', 'REQ_015'),
-- ('CC_019', TRUE, '2026-03-25', 'REQ_019'),
-- ('CC_020', TRUE, '2026-03-26', 'REQ_020'),
-- ('CC_024', TRUE, '2026-03-26', 'REQ_024'),
-- ('CC_027', TRUE, '2026-03-29', 'REQ_027'),
-- ('CC_028', TRUE, '2026-03-25', 'REQ_028'),
-- ('CC_030', TRUE, '2026-03-27', 'REQ_030'),
-- ('CC_032', TRUE, '2026-03-28', 'REQ_032'),
-- ('CC_035', TRUE, '2026-03-25', 'REQ_035'),
-- ('CC_037', TRUE, '2026-03-26', 'REQ_037'),
-- ('CC_039', TRUE, '2026-03-30', 'REQ_039'),
-- ('CC_041', TRUE, '2026-04-01', 'REQ_041'),
-- ('CC_043', TRUE, '2026-04-03', 'REQ_043'),
-- ('CC_046', TRUE, '2026-04-05', 'REQ_046'),
-- ('CC_047', TRUE, '2026-04-05', 'REQ_047'),
-- ('CC_048', TRUE, '2026-04-06', 'REQ_048'),
-- ('CC_051', TRUE, '2026-04-07', 'REQ_051'),
-- ('CC_053', TRUE, '2026-04-08', 'REQ_053'),
-- ('CC_054', TRUE, '2026-04-09', 'REQ_054'),
-- ('CC_056', TRUE, '2026-04-10', 'REQ_056');

-- use ute_forum;
-- INSERT INTO `clarificationconversation`
-- (`id`, `isopen`, `createat`, `request_id`)
-- VALUES
-- ('CC_067', TRUE, NOW(), 'REQ_67');

-- INSERT INTO `message` (`id`, `content`, `createat`, `clarificationconversation_id`, `user_id`) VALUES
-- -- Cuộc hội thoại CC_008 (Về Request REQ_008 - Sinh viên U_STU_10 & Chuyên viên DEP_KCNTT U_DEP_07)
-- ('MSG_005', 'Dạ thầy ơi, em vẫn không đăng nhập được Portal dù đã reset pass.', '2026-03-24', 'CC_008', 'U_STU_10'),
-- ('MSG_006', 'Em thử xóa cache trình duyệt hoặc dùng ẩn danh xem sao nhé.', '2026-03-24', 'CC_008', 'U_DEP_07'),
-- ('MSG_007', 'Em làm được rồi, em cảm ơn thầy!', '2026-03-24', 'CC_008', 'U_STU_10'),

-- -- Cuộc hội thoại CC_009 (Về Request REQ_009 - Sinh viên U_STU_11 & Chuyên viên DEP_KCNTT U_DEP_07)
-- ('MSG_008', 'Cho em xin lại pass wifi tòa E với ạ.', '2026-03-24', 'CC_009', 'U_STU_11'),
-- ('MSG_009', 'Chào em, pass wifi tuần này là: ute@2026 nhé.', '2026-03-25', 'CC_009', 'U_DEP_07'),

-- -- Cuộc hội thoại CC_013 (Về Request REQ_013 - Sinh viên U_STU_16 & Chuyên viên DEP_DT U_DEP_05)
-- ('MSG_010', 'Em bị sai ngày sinh trên hệ thống, em cần nộp giấy tờ gì ạ?', '2026-03-24', 'CC_013', 'U_STU_16'),
-- ('MSG_011', 'Em mang CCCD bản gốc lên phòng Đào tạo tòa A để đối chiếu nhé.', '2026-03-25', 'CC_013', 'U_DEP_05'),

-- -- Cuộc hội thoại CC_014 (Về Request REQ_014 - Sinh viên U_STU_29 & Chuyên viên DEP_DT U_DEP_05)
-- ('MSG_012', 'Thầy cho em hỏi khi nào có kết quả phúc khảo môn Toán ạ?', '2026-03-25', 'CC_014', 'U_STU_29'),
-- ('MSG_013', 'Dự kiến là sau 7 ngày làm việc kể từ ngày hết hạn nộp đơn em nhé.', '2026-03-26', 'CC_014', 'U_DEP_05'),

-- -- Cuộc hội thoại CC_018 (Về Request REQ_018 - Sinh viên U_STU_12 & Chuyên viên DEP_KDBL)
-- -- Giả định chuyên viên Khoa Điện là U_DEP_07 (hoặc admin xử lý)
-- ('MSG_014', 'Em muốn mượn Kit Arduino Uno cho bài tập lớn.', '2026-03-25', 'CC_018', 'U_STU_12'),
-- ('MSG_015', 'Em qua văn phòng Khoa gặp thầy Tuấn để ký sổ mượn thiết bị.', '2026-03-26', 'CC_018', 'U_DEP_07'),

-- -- Cuộc hội thoại CC_019 (Về Request REQ_019 - Sinh viên U_STU_13)
-- ('MSG_016', 'Xưởng điện khi nào bảo trì xong vậy ạ?', '2026-03-25', 'CC_019', 'U_STU_13'),
-- ('MSG_017', 'Thứ 2 tuần sau xưởng sẽ mở cửa lại bình thường.', '2026-03-26', 'CC_019', 'U_DEP_07'),

-- -- Cuộc hội thoại CC_023 (Về Request REQ_023 - Sinh viên U_STU_14 & Chuyên viên DEP_KCK U_DEP_08)
-- ('MSG_018', 'Em không đăng ký được ca thực hành hàn chiều nay.', '2026-03-26', 'CC_023', 'U_STU_14'),
-- ('MSG_019', 'Hệ thống đang quá tải, em tải lại trang hoặc đăng ký vào tối nay.', '2026-03-26', 'CC_023', 'U_DEP_08'),

-- -- Cuộc hội thoại CC_028 (Về Request REQ_028 - Sinh viên U_STU_17 & Chuyên viên CTSV)
-- -- Giả định chuyên viên CTSV là U_DEP_06
-- ('MSG_020', 'Em bị mất thẻ SV, em có được dùng giấy xác nhận thay thế không?', '2026-03-25', 'CC_028', 'U_STU_17'),
-- ('MSG_021', 'Được em nhé, em lên phòng CTSV để xin giấy xác nhận tạm thời.', '2026-03-26', 'CC_028', 'U_DEP_06'),

-- -- Cuộc hội thoại CC_033 (Về Request REQ_033 - Sinh viên U_STU_19 & Chuyên viên KHTC U_DEP_06)
-- ('MSG_022', 'Em lỡ đóng tiền BHYT 2 lần qua App ngân hàng.', '2026-03-29', 'CC_033', 'U_STU_19'),
-- ('MSG_023', 'Em chụp màn hình giao dịch và gửi kèm vào đây để bên tài chính đối soát.', '2026-03-30', 'CC_033', 'U_DEP_06'),

-- -- Cuộc hội thoại CC_036 (Về Request REQ_036 - Sinh viên U_STU_50 & Chuyên viên Y tế U_DEP_09)
-- ('MSG_024', 'Lớp em vẫn chưa nhận được thẻ BHYT giấy ạ.', '2026-03-31', 'CC_036', 'U_STU_50'),
-- ('MSG_025', 'Hiện tại đã có thẻ, lớp trưởng ghé trạm y tế nhận cho cả lớp nhé.', '2026-04-01', 'CC_036', 'U_DEP_09'),

-- -- Cuộc hội thoại CC_039 (Về Request REQ_039 - Sinh viên U_STU_30)
-- ('MSG_026', 'Wifi tầng 5 tòa A yếu quá thầy ơi, không load được tài liệu.', '2026-03-30', 'CC_039', 'U_STU_30'),
-- ('MSG_027', 'Bộ phận kỹ thuật đang kiểm tra lại node mạng khu vực đó.', '2026-03-31', 'CC_039', 'U_DEP_07'),

-- -- Hội thoại CC_043 (Về Request REQ_043 - Mất ví)
-- ('MSG_028', 'Em để quên ví ở canteen, ai nhặt được cho em xin lại.', '2026-04-03', 'CC_043', 'U_STU_47'),
-- ('MSG_029', 'Có bạn gửi một chiếc ví nâu ở bàn bảo vệ, em qua kiểm tra xem nhé.', '2026-04-04', 'CC_043', 'U_DEP_06'),

-- -- Hội thoại CC_051 (Về Request REQ_051 - Bảng điểm)
-- ('MSG_030', 'Em cần bảng điểm gấp để nộp học bổng trong sáng mai.', '2026-04-07', 'CC_051', 'U_STU_16'),
-- ('MSG_031', 'Hệ thống đang in, chiều nay 3h em qua phòng Đào tạo lấy.', '2026-04-07', 'CC_051', 'U_DEP_05'),

-- -- Hội thoại CC_054 (Về Request REQ_054 - Lỗi GPA)
-- ('MSG_032', 'GPA học kỳ trước của em bị lệch so với em tự tính.', '2026-04-09', 'CC_054', 'U_STU_41'),
-- ('MSG_033', 'Em liệt kê danh sách điểm các môn để phòng Đào tạo rà soát lại.', '2026-04-10', 'CC_054', 'U_DEP_05'),

-- -- Hội thoại CC_056 (Về Request REQ_056 - TOEIC)
-- ('MSG_034', 'K23 yêu cầu TOEIC bao nhiêu để tốt nghiệp ạ?', '2026-04-10', 'CC_056', 'U_STU_54'),
-- ('MSG_035', 'Hiện tại quy định là 450 TOEIC hoặc chứng chỉ tương đương em nhé.', '2026-04-11', 'CC_056', 'U_DEP_06'),

-- -- Thêm một số tin nhắn rời rạc khác để tăng volume dữ liệu
-- ('MSG_036', 'Cảm ơn thầy, em đã nắm thông tin.', '2026-03-27', 'CC_015', 'U_STU_35'),
-- ('MSG_037', 'Yêu cầu này không đủ hồ sơ nên chúng tôi xin phép từ chối.', '2026-03-27', 'CC_021', 'U_DEP_07'),
-- ('MSG_038', 'Sách này hiện đã có người mượn, bạn vui lòng quay lại sau.', '2026-03-28', 'CC_032', 'U_DEP_06'),
-- ('MSG_039', 'Hồ sơ của em đã được chuyển tiếp lên Ban giám hiệu.', '2026-03-27', 'CC_030', 'U_DEP_06'),
-- ('MSG_040', 'Em đã nộp file báo cáo đồ án lên portal chưa?', '2026-03-31', 'CC_040', 'U_DEP_07'),
-- ('MSG_041', 'Dạ em nộp rồi ạ.', '2026-04-01', 'CC_040', 'U_STU_36'),
-- ('MSG_042', 'Máy chiếu phòng C103 đã được sửa xong.', '2026-04-03', 'CC_042', 'U_DEP_08'),
-- ('MSG_043', 'Em xin nộp bổ sung chứng chỉ MOS.', '2026-04-07', 'CC_050', 'U_STU_10'),
-- ('MSG_044', 'Đã ghi nhận, hệ thống sẽ cập nhật sau 24h.', '2026-04-08', 'CC_050', 'U_DEP_07'),
-- ('MSG_045', 'Lớp hè môn Triết sẽ mở nếu đủ 40 bạn đăng ký.', '2026-04-09', 'CC_053', 'U_DEP_05'),
-- ('MSG_046', 'Cho em xin form đăng ký thực tập Intel.', '2026-04-10', 'CC_055', 'U_STU_45'),
-- ('MSG_047', 'Em tải form tại website khoa CNTT mục Biểu mẫu nhé.', '2026-04-10', 'CC_055', 'U_DEP_07'),
-- ('MSG_048', 'Mật khẩu Mail sinh viên của em là định dạng nào ạ?', '2026-04-11', 'CC_057', 'U_STU_53'),
-- ('MSG_049', 'Mặc định là ngày tháng năm sinh ddmmyyyy.', '2026-04-11', 'CC_057', 'U_DEP_05'),
-- ('MSG_050', 'Dạ em vào được rồi, cảm ơn phòng Đào tạo.', '2026-04-11', 'CC_057', 'U_STU_53');

-- 8) Bổ sung Comment (Thảo luận cộng đồng)
INSERT INTO `comment` (`id`, `content`, `request_id`, `user_id`) VALUES
-- Thảo luận về REQ_008: Lỗi đăng nhập Portal (PUBLIC)
('COM_006', 'Mình cũng bị lỗi này sáng nay, hình như hệ thống đang bảo trì.', 'REQ_008', 'U_STU_11'),
('COM_007', 'Bạn thử xóa cache trình duyệt rồi đăng nhập lại xem sao.', 'REQ_008', 'U_STU_12'),
('COM_008', 'Đã xử lý xong, các bạn vào kiểm tra lại nhé.', 'REQ_008', 'U_DEP_07'),

-- Thảo luận về REQ_010: Chuẩn đầu ra IT (PUBLIC)
('COM_009', 'Nghe nói chứng chỉ TOEFL iBT cũng được chấp nhận đó bạn.', 'REQ_010', 'U_STU_13'),
('COM_010', 'Cái này phải đợi văn phòng Khoa ra văn bản chính thức mới chắc.', 'REQ_010', 'U_STU_14'),
('COM_011', 'Đã chuyển yêu cầu này cho thầy trưởng ngành phản hồi.', 'REQ_010', 'U_DEP_07'),

-- Thảo luận về REQ_011: Lỗi phần mềm thực hành (PUBLIC)
('COM_012', 'Máy số 12 phòng 302 cũng bị lỗi tương tự, không khởi động được Docker.', 'REQ_011', 'U_STU_20'),
('COM_013', 'Hôm qua mình dùng vẫn bình thường mà ta?', 'REQ_011', 'U_STU_26'),
('COM_014', 'Kỹ thuật viên đã cài đặt lại, các bạn có thể lên test.', 'REQ_011', 'U_DEP_07'),

-- Thảo luận về REQ_012: Mượn phòng Lab làm đồ án (PUBLIC - Bị từ chối)
('COM_015', 'Tối thứ 7 tòa nhà đóng cửa sớm nên chắc không mượn được đâu nhóm ơi.', 'REQ_012', 'U_STU_10'),
('COM_016', 'Uổng quá, nhóm mình định làm xuyên đêm cho kịp tiến độ.', 'REQ_012', 'U_STU_26'),
('COM_017', 'Theo quy định mới, sinh viên không được ở lại Lab sau 21h nhé.', 'REQ_012', 'U_DEP_07'),

-- Thêm một số bình luận cho các sinh viên khác thảo luận
('COM_018', 'Portal lại lag nữa rồi mọi người ơi!', 'REQ_008', 'U_STU_21'),
('COM_019', 'Cho mình xin link tài liệu chuẩn đầu ra với bạn @U_STU_20.', 'REQ_010', 'U_STU_11'),
('COM_020', 'Phòng Lab 302 dạo này máy hơi yếu, mong Khoa nâng cấp.', 'REQ_011', 'U_STU_15');

-- 10) Announcement (Thông báo mới từ các phòng ban)
INSERT INTO `announcement` (`id`, `title`, `content`, `user_id`) VALUES
-- Thông báo từ Phòng Đào tạo (U_DEP_05)
('ANN_004', 'Thông báo lịch đăng ký học phần HK2', 'Sinh viên bắt đầu đăng ký học phần từ ngày 01/04/2026 trên hệ thống Portal.', 'U_DEP_05'),
('ANN_005', 'Danh sách thi tốt nghiệp đợt 1', 'Yêu cầu sinh viên năm cuối kiểm tra thông tin cá nhân trên danh sách niêm yết tại văn phòng Đào tạo.', 'U_DEP_05'),

-- Thông báo từ Trung tâm Tuyển sinh (U_DEP_06)
('ANN_006', 'Tư vấn hướng nghiệp tỉnh Đồng Nai', 'Trường sẽ tổ chức đoàn tư vấn tại các trường THPT khu vực Biên Hòa vào cuối tuần này.', 'U_DEP_06'),
('ANN_007', 'Mở cổng đăng ký xét tuyển học bạ', 'Thí sinh có thể nộp hồ sơ xét tuyển trực tuyến từ ngày 15/04.', 'U_DEP_06'),

-- Thông báo từ Khoa CNTT - Kỹ thuật (U_DEP_07)
('ANN_008', 'Bảo trì hệ thống Server thực hành', 'Hệ thống Server Lab tòa E sẽ tạm dừng hoạt động từ 22h tối nay để nâng cấp băng thông.', 'U_DEP_07'),
('ANN_009', 'Cuộc thi Olympic Tin học sinh viên', 'Bắt đầu nhận đơn đăng ký tham gia đội tuyển thi cấp quốc gia tại văn phòng khoa.', 'U_DEP_07'),

-- Thông báo từ Khoa Cơ khí (U_DEP_08)
('ANN_010', 'Nội quy an toàn xưởng Cơ khí', 'Yêu cầu sinh viên mặc đồ bảo hộ đầy đủ khi thực hành tại xưởng B.', 'U_DEP_08'),
('ANN_011', 'Triển lãm đồ án thiết kế máy', 'Trân trọng kính mời thầy cô và các bạn sinh viên tham quan tại sảnh tòa B sáng thứ Tư.', 'U_DEP_08'),

-- Thông báo từ Trạm Y tế (U_DEP_09)
('ANN_012', 'Khám sức khỏe định kỳ cho tân sinh viên', 'Lịch khám tập trung diễn ra tại hội trường A từ ngày 05/04 đến 10/04.', 'U_DEP_09'),
('ANN_013', 'Cấp đổi thẻ Bảo hiểm y tế lỗi', 'Những sinh viên bị sai thông tin trên thẻ BHYT vui lòng mang CCCD đến trạm y tế để chỉnh sửa.', 'U_DEP_09'),

-- Thông báo bổ sung (Tăng volume dữ liệu)
('ANN_014', 'Cảnh báo lừa đảo học phí', 'Nhà trường chỉ thu học phí qua cổng ngân hàng chính thức, không thu qua số tài khoản cá nhân.', 'U_DEP_05'),
('ANN_015', 'Hội thảo Công nghệ 4.0 và việc làm', 'Giao lưu cùng đại diện tập đoàn Intel vào chiều thứ Sáu tuần này.', 'U_DEP_07');

-- 11) FileAttachment

INSERT INTO `fileattachment` (`id`, `filename`, `fileurl`, `filetype`, `filesize`, `createat`, `request_id`, `announcement_id`) VALUES
-- FILE ĐÍNH KÈM CHO THÔNG BÁO (ANNOUNCEMENT)
('FA_005', 'huong_dan_dang_ky_hp.pdf', '/uploads/ann/guide_dk.pdf', 'application/pdf', 1245000, '2026-03-20', NULL, 'ANN_004'),
('FA_006', 'danh_sach_thi_tn_dot_1.xlsx', '/uploads/ann/ds_thi_tn.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 45000, '2026-03-20', NULL, 'ANN_005'),
('FA_007', 'so_do_tu_van_dong_nai.jpg', '/uploads/ann/map_dongnai.jpg', 'image/jpeg', 850000, '2026-03-20', NULL, 'ANN_006'),
('FA_008', 'huong_dan_xet_hoc_ba.pdf', '/uploads/ann/hd_hoc_ba.pdf', 'application/pdf', 2100000, '2026-03-20', NULL, 'ANN_007'),
('FA_009', 'noi_quy_xuong_co_khi.pdf', '/uploads/ann/noi_quy_ck.pdf', 'application/pdf', 560000, '2026-03-20', NULL, 'ANN_010'),
('FA_010', 'banner_trien_lam_do_an.png', '/uploads/ann/banner_trienlam.png', 'image/png', 3200000, '2026-03-20', NULL, 'ANN_011'),
('FA_011', 'lich_kham_suc_khoe_chi_tiet.pdf', '/uploads/ann/lich_kham_sk.pdf', 'application/pdf', 120000, '2026-03-20', NULL, 'ANN_012'),
('FA_012', 'canh_bao_lua_dao_hoc_phi.png', '/uploads/ann/warning_hack.png', 'image/png', 1100000, '2026-03-20', NULL, 'ANN_014'),
('FA_013', 'poster_hoi_thao_intel.jpg', '/uploads/ann/poster_intel.jpg', 'image/jpeg', 2500000, '2026-03-20', NULL, 'ANN_015'),

-- FILE ĐÍNH KÈM CHO YÊU CẦU (REQUEST)
-- Nhóm CNTT
('FA_014', 'loi_dang_nhap_portal.png', '/uploads/req/err_portal_01.png', 'image/png', 450000, '2026-03-24', 'REQ_008', NULL),
('FA_015', 'man_hinh_loi_docker.jpg', '/uploads/req/docker_err.jpg', 'image/jpeg', 670000, '2026-03-25', 'REQ_011', NULL),
('FA_016', 'speedtest_wifi_t5.png', '/uploads/req/wifi_speed.png', 'image/png', 150000, '2026-03-30', 'REQ_039', NULL),
('FA_017', 'chung_chi_mos_excel.pdf', '/uploads/req/mos_cert.pdf', 'application/pdf', 890000, '2026-04-07', 'REQ_050', NULL),

-- Nhóm Đào tạo
('FA_018', 'giay_khai_sinh_chinh_sua.pdf', '/uploads/req/birth_cert.pdf', 'application/pdf', 1200000, '2026-03-24', 'REQ_013', NULL),
('FA_019', 'don_xin_phuc_khao.docx', '/uploads/req/don_pk.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 25000, '2026-03-25', 'REQ_014', NULL),
('FA_020', 'lich_thi_ca_nhan_trung.png', '/uploads/req/trung_lich.png', 'image/png', 340000, '2026-03-26', 'REQ_015', NULL),
('FA_021', 'bang_diem_tam_thoi.pdf', '/uploads/req/transcript_temp.pdf', 'application/pdf', 560000, '2026-04-07', 'REQ_051', NULL),

-- Nhóm Điện & Cơ khí
('FA_022', 'danh_sach_linh_kien_muon.xlsx', '/uploads/req/list_device.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 18000, '2026-03-25', 'REQ_018', NULL),
('FA_023', 'anh_thiet_bi_hong_man_hinh.jpg', '/uploads/req/oscillo_broken.jpg', 'image/jpeg', 1100000, '2026-03-28', 'REQ_022', NULL),
('FA_024', 'bien_lai_dong_phi_xuong.pdf', '/uploads/req/receipt_xuong.pdf', 'application/pdf', 230000, '2026-03-26', 'REQ_023', NULL),
('FA_025', 'anh_phoi_thep_hong.jpg', '/uploads/req/steel_bad.jpg', 'image/jpeg', 980000, '2026-03-27', 'REQ_025', NULL),

-- Nhóm CTSV & Thư viện
('FA_026', 'don_bao_mat_the.pdf', '/uploads/req/lost_card.pdf', 'application/pdf', 140000, '2026-03-25', 'REQ_028', NULL),
('FA_027', 'giay_xac_nhan_hien_mau.jpg', '/uploads/req/blood_don.jpg', 'image/jpeg', 1500000, '2026-03-26', 'REQ_029', NULL),
('FA_028', 'so_ho_ngheo_xac_nhan.pdf', '/uploads/req/poor_cert.pdf', 'application/pdf', 3400000, '2026-03-27', 'REQ_030', NULL),
('FA_029', 'giay_ra_vien_minh_chung.pdf', '/uploads/req/hospital_cert.pdf', 'application/pdf', 850000, '2026-03-27', 'REQ_031', NULL),
('FA_030', 'anh_vi_tien_bi_mat.jpg', '/uploads/req/wallet_lost.jpg', 'image/jpeg', 420000, '2026-04-03', 'REQ_043', NULL),

-- Nhóm Tài chính & Y tế
('FA_031', 'bien_lai_bhyt_trung.pdf', '/uploads/req/bhyt_double.pdf', 'application/pdf', 210000, '2026-03-29', 'REQ_033', NULL),
('FA_032', 'don_xin_gia_han_hp.pdf', '/uploads/req/fee_extend.pdf', 'application/pdf', 130000, '2026-03-30', 'REQ_034', NULL),
('FA_033', 'giay_hen_kham_sk.png', '/uploads/req/health_check.png', 'image/png', 310000, '2026-03-25', 'REQ_035', NULL),
('FA_034', 'screenshot_loi_vi_dien_tu.png', '/uploads/req/wallet_err.png', 'image/png', 280000, '2026-04-06', 'REQ_048', NULL),

-- Nhóm khác (NCKH, Visa, Thực tập)
('FA_035', 'de_cuong_nckh_chi_tiet.pdf', '/uploads/req/nckh_proposal.pdf', 'application/pdf', 4500000, '2026-03-31', 'REQ_040', NULL),
('FA_036', 'passport_scan.pdf', '/uploads/req/passport.pdf', 'application/pdf', 5200000, '2026-04-01', 'REQ_041', NULL),
('FA_037', 'anh_may_chieu_soc_vang.jpg', '/uploads/req/projector_soc.jpg', 'image/jpeg', 1300000, '2026-04-02', 'REQ_042', NULL),
('FA_038', 'cv_xin_thuc_tap_intel.pdf', '/uploads/req/cv_intel.pdf', 'application/pdf', 450000, '2026-04-09', 'REQ_055', NULL),

-- Bổ sung thêm file cho Request để đủ số lượng
('FA_039', 'anh_the_bhyt_cu.jpg', '/uploads/req/old_bhyt.jpg', 'image/jpeg', 600000, '2026-03-31', 'REQ_036', NULL),
('FA_040', 'giay_tiep_nhan_truong_moi.pdf', '/uploads/req/transfer_confirm.pdf', 'application/pdf', 110000, '2026-03-26', 'REQ_037', NULL),
('FA_041', 'bang_diem_hoc_ba_thpt.pdf', '/uploads/req/hb_thpt.pdf', 'application/pdf', 3500000, '2026-04-01', 'REQ_038', NULL),
('FA_042', 'don_xin_tot_nghiep_som.pdf', '/uploads/req/graduate_soon.pdf', 'application/pdf', 95000, '2026-04-04', 'REQ_044', NULL),
('FA_043', 'xac_nhan_ket_qua_nckh.png', '/uploads/req/nckh_result.png', 'image/png', 1400000, '2026-03-31', 'REQ_040', NULL),
('FA_044', 'anh_cho_muon_hoi_truong.jpg', '/uploads/req/hall_e.jpg', 'image/jpeg', 2200000, '2026-04-06', 'REQ_049', NULL),
('FA_045', 'loi_gpa_he_thong.png', '/uploads/req/gpa_error.png', 'image/png', 110000, '2026-04-09', 'REQ_054', NULL),
('FA_046', 'don_xin_cap_lai_mssv.pdf', '/uploads/req/mssv_reissue.pdf', 'application/pdf', 85000, '2026-04-10', 'REQ_057', NULL),
('FA_047', 'chung_chi_toeic_450.jpg', '/uploads/req/toeic_450.jpg', 'image/jpeg', 1200000, '2026-04-10', 'REQ_056', NULL),
('FA_048', 'ke_hoach_hoc_he.pdf', '/uploads/req/summer_plan.pdf', 'application/pdf', 540000, '2026-04-08', 'REQ_053', NULL),
('FA_049', 'anh_loi_dang_ky_sach.png', '/uploads/req/lib_err.png', 'image/png', 210000, '2026-04-08', 'REQ_052', NULL),
('FA_050', 'chung_nhan_gpa_thang_4.pdf', '/uploads/req/gpa_cert.pdf', 'application/pdf', 120000, '2026-04-04', 'REQ_044', NULL),
('FA_051', 'don_xin_xac_nhan_sv.pdf', '/uploads/req/sv_confirm.pdf', 'application/pdf', 45000, '2026-04-04', 'REQ_045', NULL),
('FA_052', 'phieu_dang_ky_thi_av.docx', '/uploads/req/av_exam_form.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 32000, '2026-04-05', 'REQ_046', NULL),
('FA_053', 'bang_diem_ren_luyen_hk1.xlsx', '/uploads/req/drl_hk1.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 28000, '2026-04-05', 'REQ_047', NULL),
('FA_054', 'anh_vouchers_thi_toeic.jpg', '/uploads/req/toeic_vouch.jpg', 'image/jpeg', 950000, '2026-04-10', 'REQ_056', NULL);

-- 13) Notification (Thêm các loại thông báo khác nhau)
INSERT INTO `notification` (`id`, `content`, `notificationtype`, `title`, `isread`, `createat`) VALUES
-- 1. Chuyển sang SYSTEM_ANNOUNCEMENT_NOTIFICATION (Chỉ được 1 dòng duy nhất)
('NOTI_004', 'Thông báo mới: Lịch đăng ký học phần học kỳ phụ đã mở.', 'SYSTEM_ANNOUNCEMENT_NOTIFICATION', 'Thông báo đào tạo', FALSE, '2026-04-01'),
('NOTI_005', 'Cảnh báo: Bảo trì băng thông tòa nhà A vào tối nay.', 'NEW_ANNOUNCEMENT_NOTIFICATION', 'Hệ thống kỹ thuật', FALSE, '2026-04-01'),
('NOTI_013', 'Chuyên viên Đào tạo vừa gửi tin nhắn cho bạn (REQ_008).', 'MESSAGE_NEW_NOTIFICATION', 'Tin nhắn hỗ trợ', FALSE, '2026-03-24'),
('NOTI_018', 'Yêu cầu REQ_004 của bạn đã được chuyển sang RESOLVED.', 'FEEDBACK_RESOLVED_NOTIFICATION', 'Cập nhật tiến độ', TRUE, '2026-03-21'),
('NOTI_019', 'Yêu cầu REQ_007 của bạn đã bị REJECTED.', 'FEEDBACK_REJECTED_NOTIFICATION', 'Cập nhật tiến độ', TRUE, '2026-03-23'),
('NOTI_020', 'Yêu cầu REQ_006 đã được chuyển tiếp (FORWARDING) đến KHTC.', 'FEEDBACK_PROCESSING_NOTIFICATION', 'Cập nhật tiến độ', FALSE, '2026-03-22'),
('NOTI_009', 'Có bình luận mới trong yêu cầu REQ_008 của bạn.', 'COMMENT_ANNOUNCEMENT_NOTIFICATION', 'Tương tác mới', FALSE, '2026-03-24'),-- 8. Chuyển sang FEEDBACK_FORWARDED_TO_YOU
('NOTI_036', 'Yêu cầu REQ_031 đã chuyển tiếp đến văn phòng khoa.', 'FEEDBACK_FORWARDED_TO_YOU', 'Cập nhật tiến độ', FALSE, '2026-03-31'),
('NOTI_027', 'Cảnh báo bảo mật: Có thiết bị lạ đăng nhập tài khoản bạn.', 'ADMIN_NOTIFICATION', 'Bảo mật hệ thống', FALSE, '2026-04-10');

-- 14) Gửi thông báo cho nhiều user
INSERT INTO `userreceivenotification` (`userid`, `notificationid`) VALUES
-- 1. Thông báo hệ thống (Gửi cho nhiều sinh viên cùng lúc)
('U_STU_10', 'NOTI_004'),
('U_STU_11', 'NOTI_004'),
('U_STU_12', 'NOTI_004'),
('U_STU_13', 'NOTI_004'),

-- 2. Thông báo bảo trì (Gửi cho nhóm sinh viên khoa CNTT)
('U_STU_10', 'NOTI_005'),
('U_STU_11', 'NOTI_005'),
('U_STU_20', 'NOTI_005'),

-- 3. Tin nhắn hỗ trợ cho REQ_008 (Gửi đích danh cho Trần Thành Tâm)
('U_STU_10', 'NOTI_013'),

-- 4. Cập nhật trạng thái RESOLVED cho REQ_004 (Giả định gửi cho Nam - U_STU_03 nếu có, 
-- hoặc gán tạm cho U_STU_16 để test)
('U_STU_16', 'NOTI_018'),

-- 5. Cập nhật trạng thái REJECTED cho REQ_007
('U_STU_17', 'NOTI_019'),

-- 6. Cập nhật trạng thái FORWARDING cho REQ_006
('U_STU_19', 'NOTI_020'),

-- 7. Thông báo bình luận mới trong REQ_008
('U_STU_10', 'NOTI_009'),

-- 8. Thông báo chuyển tiếp yêu cầu REQ_031
('U_STU_18', 'NOTI_036'),

-- 9. Cảnh báo bảo mật (Gửi đích danh cho user bị nghi ngờ)
('U_STU_40', 'NOTI_027'),

-- Gửi thêm một số thông báo hệ thống cho các user khác để đủ volume dữ liệu test
('U_STU_50', 'NOTI_004'),
('U_STU_51', 'NOTI_004'),
('U_STU_52', 'NOTI_004'),
('U_STU_53', 'NOTI_004'),
('U_STU_54', 'NOTI_004');

INSERT INTO `department` (`id`, `description`, `email`, `isactive`, `location`, `name`, `phone`) VALUES
('DEP_KCNTT2', NULL, 'fit@hcmute.edu.vn', 1, 'A1-301', 'Khoan Công Nghệ Thông Tin', '0999234563');



INSERT INTO `users` (`id`, `fullname`, `email`, `password`, `role`, `department_id`) VALUES
('23110161', 'Le Van Dao Tao', '23110161@student.hcmute.edu.vn', '$2a$12$2zFTyFiQqtpbWIcNDkNwNeaYVyyP36WbTfs/LnzhwdXr1fQg9eTUq', 'ROLE_STUDENT', NULL),
('admin', 'Admin', 'admin@hcmute.edu.vn', '$2a$12$2.OorPW3RfnEI7yOWmeHKufRXPRkmL8OQRpX1saxQ423wYWqcvfp2', 'ROLE_ADMIN', NULL),
('GV110001', 'Ngo Van Ky Thuat', '110001@teacher.hcmute.edu.vn', '$2a$12$MeINwziSLhSOHDle6k6R..tLgmLs9hV7Cmo1umLGeDvQIoHYCEx8u', 'ROLE_DEPARTMENT', 'DEP_KCNTT');


INSERT INTO `request` (`id`, `subject`, `description`, `currentstatus`, `timecreate`, `poststatus`, `department_id`, `user_id`) VALUES
('REQ_060', 'Lỗi đăng nhập LMS mới', 'Không truy cập được hệ thống LMS phiên bản mới', 'RESOLVED', '2026-04-11 08:00:00', 'PUBLIC', 'DEP_KCNTT', '23110161'),
('REQ_061', 'Xin chuyển lớp học phần', 'Muốn chuyển sang lớp khác do trùng lịch', 'APPROVED', '2026-04-11 09:00:00', 'PUBLIC', 'DEP_DT', '23110161'),
('REQ_062', 'Gia hạn đóng học phí', 'Xin gia hạn thêm 1 tuần do hoàn cảnh gia đình', 'REJECTED', '2026-04-11 10:00:00', 'PRIVATE', 'DEP_KHTC', '23110161'),
('REQ_063', 'Cấp lại thẻ sinh viên', 'Bị mất thẻ sinh viên tại căn tin', 'RESOLVED', '2026-04-11 11:00:00', 'PUBLIC', 'DEP_CTSV', '23110161'),
('REQ_064', 'Hỏi về chứng chỉ TOEIC', 'Cần biết mức TOEIC tối thiểu để tốt nghiệp', 'FORWARDING', '2026-04-11 12:00:00', 'PUBLIC', 'DEP_DT', '23110161'),
('REQ_065', 'Sửa lỗi wifi tầng 3', 'Wifi chập chờn không ổn định', 'RESOLVED', '2026-04-11 13:00:00', 'PUBLIC', 'DEP_KCNTT', '23110161');

INSERT INTO requeststatushistory (id, status, createat, request_id) VALUES
-- REQ_060 (RESOLVED)
('RSH_060_01','PENDING','2026-04-11 08:00:00','REQ_060'),
('RSH_060_02','FORWARDING','2026-04-11 08:30:00','REQ_060'),
('RSH_060_03','APPROVED','2026-04-11 10:00:00','REQ_060'),
('RSH_060_04','RESOLVED','2026-04-11 15:00:00','REQ_060'),

-- REQ_061 (APPROVED)
('RSH_061_01','PENDING','2026-04-11 09:00:00','REQ_061'),
('RSH_061_02','FORWARDING','2026-04-11 09:20:00','REQ_061'),
('RSH_061_03','APPROVED','2026-04-11 11:00:00','REQ_061'),

-- REQ_062 (REJECTED)
('RSH_062_01','PENDING','2026-04-11 10:00:00','REQ_062'),
('RSH_062_02','FORWARDING','2026-04-11 10:30:00','REQ_062'),
('RSH_062_03','REJECTED','2026-04-11 13:00:00','REQ_062'),

-- REQ_063 (RESOLVED)
('RSH_063_01','PENDING','2026-04-11 11:00:00','REQ_063'),
('RSH_063_02','FORWARDING','2026-04-11 11:20:00','REQ_063'),
('RSH_063_03','APPROVED','2026-04-11 13:30:00','REQ_063'),
('RSH_063_04','RESOLVED','2026-04-11 16:00:00','REQ_063'),

-- REQ_064 (FORWARDING)
('RSH_064_01','PENDING','2026-04-11 12:00:00','REQ_064'),
('RSH_064_02','FORWARDING','2026-04-11 12:15:00','REQ_064'),

-- REQ_065 (RESOLVED)
('RSH_065_01','PENDING','2026-04-11 13:00:00','REQ_065'),
('RSH_065_02','FORWARDING','2026-04-11 13:25:00','REQ_065'),
('RSH_065_03','APPROVED','2026-04-11 15:00:00','REQ_065'),
('RSH_065_04','RESOLVED','2026-04-11 18:00:00','REQ_065');

INSERT INTO `clarificationconversation` (`id`, `isopen`, `createat`, `request_id`,`subject`) VALUES
-- Nhóm kết thúc (isopen = FALSE)
('CC_100', TRUE, '2026-03-24', 'REQ_060','Lỗi LMS');

INSERT INTO forwardinglog (id, message, note, forwardat, fromdepartment_id, todepartment_id, request_id, user_id) VALUES
('FL_060','Chuyển xử lý LMS','Chuyển sang phòng kỹ thuật hệ thống','2026-04-11 08:30:00','DEP_CTSV','DEP_KCNTT','REQ_060','U_DEP_07'),

('FL_061','Chuyển xử lý học vụ','Chuyển sang phòng đào tạo','2026-04-11 09:20:00','DEP_KCNTT','DEP_DT','REQ_061','U_DEP_05'),

('FL_062','Chuyển xét duyệt tài chính','Chuyển sang phòng kế hoạch tài chính','2026-04-11 10:30:00','DEP_KCNTT','DEP_KHTC','REQ_062','U_DEP_06'),

('FL_063','Chuyển cấp lại thẻ','Chuyển sang phòng CTSV','2026-04-11 11:20:00','DEP_KHTC','DEP_CTSV','REQ_063','U_DEP_06'),

('FL_064','Chuyển tư vấn TOEIC','Chuyển sang trung tâm tuyển sinh','2026-04-11 12:15:00','DEP_CTSV','DEP_DT','REQ_064','U_DEP_05'),

('FL_065','Chuyển kiểm tra wifi','Chuyển sang bộ phận kỹ thuật','2026-04-11 13:25:00','DEP_DT','DEP_KCNTT','REQ_065','U_DEP_07');
COMMIT;