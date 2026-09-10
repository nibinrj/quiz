package com.nibin.quiz.Controller;

import com.nibin.quiz.DTO.AttemptResultDTO;
import com.nibin.quiz.DTO.QuizStartDTO;
import com.nibin.quiz.DTO.QuizSubmissionDTO;
import com.nibin.quiz.Service.QuizAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/student")
@CrossOrigin
@Slf4j
public class QuizAttemptController {

    @Autowired
    private QuizAttemptService service;

    @GetMapping("/quiz/{quizId}/start")
    public ResponseEntity<QuizStartDTO> start(@PathVariable Integer quizId, Principal principal) {
        log.info("Start requested for quiz {} by {}", quizId, principal.getName());
        return ResponseEntity.ok(service.startQuiz(quizId, principal.getName()));
    }

    @PostMapping("/quiz/submit")
    public ResponseEntity<Map<String, Integer>> submit(@RequestBody QuizSubmissionDTO submission) {
        log.info("Submission received for attempt {}", submission.getAttemptId());
        return ResponseEntity.ok(Map.of("score", service.submitQuiz(submission)));
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<AttemptResultDTO> attempt(@PathVariable String attemptId) {
        log.info("Attempt fetch requested for {}", attemptId);
        return ResponseEntity.ok(service.getAttempt(attemptId));
    }
}
