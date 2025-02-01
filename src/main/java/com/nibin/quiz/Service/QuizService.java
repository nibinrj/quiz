package com.nibin.quiz.Service;


import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Repository.QuizRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    @Autowired
    QuizRepo repo;

    public List<Question> getallQuestions() {
        return repo.findAll();
    }

    public Question addQuestion(Question question) {
        return repo.save(question);
    }

    public void delete(int id)
    {
        repo.deleteById(id);
    }
}
