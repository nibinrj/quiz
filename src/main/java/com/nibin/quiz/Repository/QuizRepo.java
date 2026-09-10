package com.nibin.quiz.Repository;

import com.nibin.quiz.Model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface QuizRepo extends JpaRepository<Question,Integer> {

    /**
     * Questions of one quiz with their options already joined, so building the student
     * view costs one statement instead of one per question.
     */
    @Query("select distinct q from Question q left join fetch q.options where q.quiz.id = :quizId")
    List<Question> findAllWithOptionsByQuizId(@Param("quizId") Integer quizId);

    /**
     * Hydrates the options collection for a known set of questions in a single statement.
     */
    @Query("select distinct q from Question q left join fetch q.options where q.id in :ids")
    List<Question> findAllWithOptionsByIdIn(@Param("ids") Collection<Integer> ids);
}
