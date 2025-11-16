package com.nibin.quiz.Service;


import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Repository.QuizRepo;
import com.nibin.quiz.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public List<Question> addManyQuestion(List<Question> questions) {
        List<Question> questions1 = repo.saveAll(questions);
        return questions1;
    }
}
