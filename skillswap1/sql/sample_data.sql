USE skillswap_db;

-- Skill catalogue
INSERT IGNORE INTO skills (skill_name, category) VALUES
('Java Programming', 'Technical'),
('Python', 'Technical'),
('Guitar', 'Music'),
('Photoshop', 'Design'),
('Public Speaking', 'Soft Skill'),
('Video Editing', 'Design'),
('Spoken French', 'Language'),
('Chess', 'Games');

-- Demo students. Password for every demo account below is "pass123"
-- (SHA-256 hash: 9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c)
INSERT IGNORE INTO users (reg_no, name, email, password_hash, role, year_of_study, department) VALUES
('RA2511003030200', 'Priyanshu Sharma', 'ps0612@srmist.edu.in',
 '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'STUDENT', 2, 'CSE CORE'),
('RA2511002', 'Ananya Rao', 'ananya@srmist.edu.in',
 '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'STUDENT', 2, 'CSE CORE'),
('RA2511003', 'Kabir Mehta', 'kabir@srmist.edu.in',
 '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'STUDENT', 3, 'ECE'),
('RA2511004', 'Diya Nair', 'diya@srmist.edu.in',
 '9b8769a4a742959a2d0298c36fb70623f2dfacda8436237df08d8dfd5b37374c', 'STUDENT', 1, 'IT');

-- Priyanshu teaches Java, wants to learn Guitar
INSERT IGNORE INTO user_teach_skills (user_id, skill_id, proficiency)
SELECT u.user_id, s.skill_id, 'ADVANCED' FROM users u, skills s
WHERE u.reg_no='RA2511003030200' AND s.skill_name='Java Programming';

INSERT IGNORE INTO user_learn_skills (user_id, skill_id, desired_level)
SELECT u.user_id, s.skill_id, 'BEGINNER' FROM users u, skills s
WHERE u.reg_no='RA2511003030200' AND s.skill_name='Guitar';

-- Ananya teaches Guitar, wants to learn Java
INSERT IGNORE INTO user_teach_skills (user_id, skill_id, proficiency)
SELECT u.user_id, s.skill_id, 'EXPERT' FROM users u, skills s
WHERE u.reg_no='RA2511002' AND s.skill_name='Guitar';

INSERT IGNORE INTO user_learn_skills (user_id, skill_id, desired_level)
SELECT u.user_id, s.skill_id, 'INTERMEDIATE' FROM users u, skills s
WHERE u.reg_no='RA2511002' AND s.skill_name='Java Programming';

-- Kabir teaches Photoshop, wants Public Speaking
INSERT IGNORE INTO user_teach_skills (user_id, skill_id, proficiency)
SELECT u.user_id, s.skill_id, 'ADVANCED' FROM users u, skills s
WHERE u.reg_no='RA2511003' AND s.skill_name='Photoshop';

INSERT IGNORE INTO user_learn_skills (user_id, skill_id, desired_level)
SELECT u.user_id, s.skill_id, 'BEGINNER' FROM users u, skills s
WHERE u.reg_no='RA2511003' AND s.skill_name='Public Speaking';

-- Diya teaches Public Speaking, wants Photoshop
INSERT IGNORE INTO user_teach_skills (user_id, skill_id, proficiency)
SELECT u.user_id, s.skill_id, 'INTERMEDIATE' FROM users u, skills s
WHERE u.reg_no='RA2511004' AND s.skill_name='Public Speaking';

INSERT IGNORE INTO user_learn_skills (user_id, skill_id, desired_level)
SELECT u.user_id, s.skill_id, 'BEGINNER' FROM users u, skills s
WHERE u.reg_no='RA2511004' AND s.skill_name='Photoshop';
