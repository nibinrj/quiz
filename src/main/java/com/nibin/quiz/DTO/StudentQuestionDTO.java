package com.nibin.quiz.DTO;

import lombok.Data;

import java.util.List;

@Data
public class StudentQuestionDTO {
    private Integer questionId;
    private String questionText;
    private List<String> options;
    // NOTICE: "answer" and "isCorrect" are completely missing here!
}