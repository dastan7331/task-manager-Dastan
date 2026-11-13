INSERT INTO users (name, email) VALUES
 ('Asel', 'asel@example.com'), ('Begzada', 'begzada@example.com'), ('Nurlan', 'nurlan@example.com');
INSERT INTO categories (name) VALUES ('Study'), ('Work'), ('Personal');
INSERT INTO projects (name, owner_id) VALUES ('OOP course', 1), ('Group project', 2);
INSERT INTO tasks (title, description, status, priority, due_date, project_id, assignee_id, category_id) VALUES
 ('Read Spring docs', 'Controllers and repositories', 'DONE', 3, '2026-11-05', 1, 1, 1),
 ('Prepare ER diagram', 'For the course project', 'IN_PROGRESS', 5, '2026-11-12', 1, 1, 1),
 ('Write README', NULL, 'TODO', 2, '2026-11-20', 2, 2, 2),
 ('Fix login bug', 'Validation of email', 'TODO', 4, '2026-11-01', 2, 3, 2);
INSERT INTO comments (task_id, author_id, text) VALUES
 (2, 2, 'Do not forget 3NF'), (4, 1, 'This one is overdue');
