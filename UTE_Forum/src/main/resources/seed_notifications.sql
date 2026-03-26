-- Seed notifications for student UI demo
-- Run this script in MySQL after application tables are created.

INSERT INTO notification (id, content, notificationtype, title, isread, createat)
VALUES
('NTF_FEEDBACK_SUBMITTED_01', 'Gop y "Hoc phi nam hoc moi" cua ban da duoc gui thanh cong.', 'FEEDBACK_SUBMITTED_NOTIFICATION', 'Gop y thanh cong', 0, CURDATE()),
('NTF_FEEDBACK_PROCESSING_01', 'Gop y "Giao trinh" cua ban dang duoc xu ly.', 'FEEDBACK_PROCESSING_NOTIFICATION', 'Dang xu ly gop y', 1, DATE_SUB(CURDATE(), INTERVAL 2 MONTH)),
('NTF_FEEDBACK_RESOLVED_01', 'Gop y "Giao trinh" cua ban da duoc giai quyet.', 'FEEDBACK_RESOLVED_NOTIFICATION', 'Gop y da hoan tat', 1, DATE_SUB(CURDATE(), INTERVAL 2 MONTH)),
('NTF_COMMENT_FORUM_01', 'Co binh luan moi tren bai dang: "Em oi, diem da nhap roi nha em"', 'COMMENT_FORUM_POST_NOTIFICATION', 'Binh luan moi', 1, DATE_SUB(CURDATE(), INTERVAL 2 MONTH))
ON DUPLICATE KEY UPDATE
content = VALUES(content),
title = VALUES(title),
isread = VALUES(isread),
createat = VALUES(createat);

-- Map notifications to one student account (first ROLE_STUDENT found).
INSERT IGNORE INTO userreceivenotification (userid, notificationid)
SELECT u.id, n.id
FROM users u
JOIN notification n ON n.id IN (
    'NTF_FEEDBACK_SUBMITTED_01',
    'NTF_FEEDBACK_PROCESSING_01',
    'NTF_FEEDBACK_RESOLVED_01',
    'NTF_COMMENT_FORUM_01'
)
WHERE u.role = 'ROLE_STUDENT'
LIMIT 4;

