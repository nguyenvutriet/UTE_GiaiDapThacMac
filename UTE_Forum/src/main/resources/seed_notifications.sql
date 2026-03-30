-- Seed notifications for all roles (student/staff/admin).
-- This script is idempotent and safe to run many times.

INSERT INTO notification (id, content, notificationtype, title, isread, createat)
VALUES
('NTF_STU_FEEDBACK_SUBMITTED_01', 'Gop y "Hoc phi nam hoc moi" cua ban da duoc gui thanh cong.', 'FEEDBACK_SUBMITTED_NOTIFICATION', 'Gop y thanh cong', 0, CURDATE()),
('NTF_STU_FEEDBACK_PROCESSING_01', 'Gop y "Giao trinh" cua ban dang duoc xu ly.', 'FEEDBACK_PROCESSING_NOTIFICATION', 'Dang xu ly gop y', 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY)),
('NTF_STU_FEEDBACK_RESOLVED_01', 'Gop y "Giao trinh" cua ban da duoc giai quyet.', 'FEEDBACK_RESOLVED_NOTIFICATION', 'Gop y da hoan tat', 1, DATE_SUB(CURDATE(), INTERVAL 30 DAY)),
('NTF_STU_COMMENT_FORUM_01', 'Co binh luan moi tren bai dang: "Em oi, diem da nhap roi nha em"', 'COMMENT_FORUM_POST_NOTIFICATION', 'Binh luan moi', 0, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),

('NTF_STAFF_NEW_FEEDBACK_01', 'Co gop y moi can phong ban tiep nhan.', 'NEW_FEEDBACK_RECEIVED', 'Tiep nhan gop y moi', 0, CURDATE()),
('NTF_STAFF_FEEDBACK_FORWARDED_01', 'Mot gop y da duoc chuyen den phong ban cua ban.', 'FEEDBACK_FORWARDED_TO_YOU', 'Gop y duoc chuyen den', 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY)),
('NTF_STAFF_REPORT_NOTE_01', 'Yeu cau can cap nhat tien do xu ly gop y.', 'ADMIN_NOTIFICATION', 'Nhac cap nhat tien do', 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),

('NTF_ADMIN_REPORT_REVIEW_01', 'He thong co bao cao moi can duyet.', 'NEW_COMMENT_REPORT_FOR_ADMIN', 'Bao cao moi', 0, CURDATE()),
('NTF_ADMIN_SYSTEM_ANNOUNCE_01', 'Thong bao he thong dinh ky da duoc tao.', 'SYSTEM_ANNOUNCEMENT_NOTIFICATION', 'Thong bao he thong', 1, DATE_SUB(CURDATE(), INTERVAL 7 DAY)),
('NTF_ADMIN_FEEDBACK_AUDIT_01', 'Tong hop thong ke gop y theo ngay da san sang.', 'REPORT_RESOLVED_NO_VIOLATION', 'Thong ke gop y', 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY))
ON DUPLICATE KEY UPDATE
content = VALUES(content),
title = VALUES(title),
isread = VALUES(isread),
createat = VALUES(createat);

INSERT IGNORE INTO userreceivenotification (userid, notificationid)
SELECT u.id, n.id
FROM users u
JOIN notification n ON n.id IN (
    'NTF_STU_FEEDBACK_SUBMITTED_01',
    'NTF_STU_FEEDBACK_PROCESSING_01',
    'NTF_STU_FEEDBACK_RESOLVED_01',
    'NTF_STU_COMMENT_FORUM_01'
)
WHERE u.role = 'ROLE_STUDENT';

INSERT IGNORE INTO userreceivenotification (userid, notificationid)
SELECT u.id, n.id
FROM users u
JOIN notification n ON n.id IN (
    'NTF_STAFF_NEW_FEEDBACK_01',
    'NTF_STAFF_FEEDBACK_FORWARDED_01',
    'NTF_STAFF_REPORT_NOTE_01'
)
WHERE u.role = 'ROLE_DEPARTMENT';

INSERT IGNORE INTO userreceivenotification (userid, notificationid)
SELECT u.id, n.id
FROM users u
JOIN notification n ON n.id IN (
    'NTF_ADMIN_REPORT_REVIEW_01',
    'NTF_ADMIN_SYSTEM_ANNOUNCE_01',
    'NTF_ADMIN_FEEDBACK_AUDIT_01'
)
WHERE u.role = 'ROLE_ADMIN';

UPDATE notification n
SET n.referenceid = (
    SELECT r.id
    FROM request r
    WHERE r.poststatus = 'PUBLIC'
    ORDER BY r.timecreate DESC
    LIMIT 1
)
WHERE n.notificationtype IN (
    'VOTE_FORUM_POST_NOTIFICATION',
    'COMMENT_FORUM_POST_NOTIFICATION',
    'REPLY_COMMENT_FORUM_POST_NOTIFICATION'
);

