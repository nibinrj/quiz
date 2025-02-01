package com.nibin.quiz.Repository;

import com.nibin.quiz.Model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuizRepo extends JpaRepository<Question,Integer> {
}
