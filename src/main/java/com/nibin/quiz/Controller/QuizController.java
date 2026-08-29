package com.nibin.quiz.Controller;

import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Service.QuizService;
import lombok.extern.slf4j.Slf4j; // 1. Import this
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@CrossOrigin
@Slf4j // 2. Add this annotation. It creates a 'log' object for you.
public class QuizController {

    @Autowired
    private QuizService service;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getallQuestion() {
        log.info("Request received to fetch all questions"); // Info level

        return new ResponseEntity<>(service.getallQuestions(), HttpStatus.OK);
    }

    @PostMapping("/question/add")
    public ResponseEntity<Question> addQuestion(@RequestBody Question question) {
        log.info("Request to add new question: {}", question.getQuestion_text());

        Question savedQuestion = service.addQuestion(question);

        log.debug("Full question object saved: {}", savedQuestion); // Debug level (detailed)
        return new ResponseEntity<>(savedQuestion, HttpStatus.OK);
    }

    @PostMapping("/question/addMany")
    public ResponseEntity<List<Question>> addManyQuestion(@RequestBody List<Question> questions) {
        log.info("Received request to bulk add {} questions", questions.size());
        return new ResponseEntity<>(service.addManyQuestion(questions), HttpStatus.OK);
    }

    @DeleteMapping("/question/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        log.warn("Request received to DELETE question with ID: {}", id); // Warn level
        service.delete(id);
        return ResponseEntity.ok("Question deleted");
    }
}