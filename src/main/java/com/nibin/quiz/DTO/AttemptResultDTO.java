package com.nibin.quiz.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AttemptResultDTO {
    private String attemptId;
    private String quizTitle;
    private String studentName;
    private Integer score;
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Integer questionId;
        private String questionText;
        private List<String> options;
        private String selectedOption;
        private boolean correct;
    }
}
