package com.nibin.quiz.DTO;

import lombok.Data;
import java.util.List;

@Data
public class QuizSubmissionDTO {
    private String attemptId; // We match this to the open session
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Integer questionId;
        private String selectedOption;
    }
}