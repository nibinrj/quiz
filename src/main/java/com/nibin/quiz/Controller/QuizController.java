package com.nibin.quiz.Controller;


import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/v1")
public class QuizController {

    @Autowired
    private QuizService service;

    @GetMapping("/student/getall")
    public ResponseEntity<List<Question>> getallQuestion()
    {
        return new ResponseEntity<List<Question>>(service.getallQuestions(),HttpStatus.OK);
    }

    @PostMapping("/admin/add")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question)
    {
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println(question.toString());
        System.out.println("-----------------------------------------------------------------------------------");
        return new ResponseEntity<Question>(service.addQuestion(question),HttpStatus.OK);

    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable int id)
    {
        service.delete(id);
        return ResponseEntity.ok("Question deleted");
    }






}
