-- Indexes required by the joins on the quiz-fetch and attempt-fetch paths.
--
-- There is no migration runner in this project (no Flyway, no Liquibase; schema comes from
-- spring.jpa.hibernate.ddl-auto=update), so this file is NOT applied automatically.
-- Run it by hand against each environment:
--     psql "$DB_URL" -f src/main/resources/db/indexes.sql
--
-- PostgreSQL creates indexes for PRIMARY KEY and UNIQUE constraints but NOT for foreign
-- keys, so every FK joined below needs one explicitly.

-- Question.quiz (@ManyToOne quiz_id)
-- Drives: QuizRepo.findAllWithOptionsByQuizId -> "where q.quiz_id = ?"
CREATE INDEX IF NOT EXISTS idx_question_quiz_id
    ON question (quiz_id);

-- Question.options (@ElementCollection -> question_options)
-- Drives: the "left join fetch q.options" on both fetch paths.
CREATE INDEX IF NOT EXISTS idx_question_options_question_id
    ON question_options (question_id);

-- AttemptAnswer.attempt (@ManyToOne attempt_id)
-- Drives: the "left join fetch a.answers" in QuizAttemptRepo.findByIdWithAnswers.
CREATE INDEX IF NOT EXISTS idx_attempt_answer_attempt_id
    ON attempt_answer (attempt_id);

-- AttemptAnswer.question (@ManyToOne question_id)
-- Drives: the "left join fetch ans.question" in QuizAttemptRepo.findByIdWithAnswers.
CREATE INDEX IF NOT EXISTS idx_attempt_answer_question_id
    ON attempt_answer (question_id);

-- QuizAttempt.quiz (@ManyToOne quiz_id)
-- Drives: the "join fetch a.quiz" in QuizAttemptRepo.findByIdWithAnswers.
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_quiz_id
    ON quiz_attempt (quiz_id);

-- QuizAttemptRepo.findByStudentIdAndQuizIdAndCompletedAtIsNull, on the quiz-fetch path.
-- Partial: only open attempts are ever looked up this way, and there is at most one per
-- (student, quiz), so the index stays small no matter how many attempts accumulate.
-- The leading user_id column also serves the "join fetch a.student" lookups.
CREATE INDEX IF NOT EXISTS idx_quiz_attempt_open_by_student_quiz
    ON quiz_attempt (user_id, quiz_id)
    WHERE completed_at IS NULL;

-- Users.username - UserRepo.findByusername, first statement on the quiz-fetch path and on
-- every authenticated request via MyUserDetailsService.
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username
    ON users (username);
