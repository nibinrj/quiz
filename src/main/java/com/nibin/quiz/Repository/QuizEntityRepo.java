package com.nibin.quiz.Repository;

import com.nibin.quiz.Model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the {@link Quiz} aggregate.
 * Named QuizEntityRepo because QuizRepo is already taken by the Question repository.
 */
@Repository
public interface QuizEntityRepo extends JpaRepository<Quiz, Integer> {
}
