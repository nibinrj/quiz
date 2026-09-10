package com.nibin.quiz.Repository;

import com.nibin.quiz.Model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizAttemptRepo extends JpaRepository<QuizAttempt, String> {
    // Find an active attempt for a specific student and quiz
    Optional<QuizAttempt> findByStudentIdAndQuizIdAndCompletedAtIsNull(Integer studentId, Integer quizId);

    /**
     * Loads an attempt with its student, quiz, answers and each answer's question in one
     * statement. Only `answers` is a bag, so this stays clear of MultipleBagFetchException;
     * the question options still need a second statement.
     */
    @Query("""
            select a from QuizAttempt a
            join fetch a.student
            join fetch a.quiz
            left join fetch a.answers ans
            left join fetch ans.question
            where a.id = :id
            """)
    Optional<QuizAttempt> findByIdWithAnswers(@Param("id") String id);
}
