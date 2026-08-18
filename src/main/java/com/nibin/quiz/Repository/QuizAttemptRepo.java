package com.nibin.quiz.Repository;

import com.nibin.quiz.Model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuizAttemptRepo extends JpaRepository<QuizAttempt, String> {
    // Find an active attempt for a specific student and quiz
    Optional<QuizAttempt> findByStudentIdAndQuizIdAndCompletedAtIsNull(Integer studentId, Integer quizId);
}