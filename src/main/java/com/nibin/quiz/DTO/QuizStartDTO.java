package com.nibin.quiz.DTO;

import lombok.Data;

import java.util.List;

@Data
public class QuizStartDTO {
    private String attemptId; // The UUID of the session
    private String title;
    private Integer timeLimitMinutes;
    private List<QuestionDTO> questions;

    @Data
    public static class QuestionDTO {
        private Integer questionId;
        private String text;
        private List<String> options; // Just the strings, no "isCorrect" flag
    }
}