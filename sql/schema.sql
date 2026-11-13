DROP TABLE IF EXISTS comments, tasks, projects, categories, users CASCADE;

CREATE TABLE users (
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE projects (
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    owner_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tasks (
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'TODO'
                CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE')),
    priority    INT NOT NULL CHECK (priority BETWEEN 1 AND 5),
    due_date    DATE,
    project_id  INT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    assignee_id INT REFERENCES users(id) ON DELETE SET NULL,
    category_id INT REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE comments (
    id         SERIAL PRIMARY KEY,
    task_id    INT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id  INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    text       TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
