package com.nibin.quiz.Service;

import com.nibin.quiz.DTO.AttemptResultDTO;
import com.nibin.quiz.DTO.QuizStartDTO;
import com.nibin.quiz.DTO.QuizSubmissionDTO;
import com.nibin.quiz.Model.AttemptAnswer;
import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.QuizAttempt;
import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.QuizAttemptRepo;
import com.nibin.quiz.Repository.QuizEntityRepo;
import com.nibin.quiz.Repository.QuizRepo;
import com.nibin.quiz.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    @Autowired
    QuizEntityRepo quizRepo;

    @Autowired
    QuizRepo questionRepo;

    @Autowired
    QuizAttemptRepo attemptRepo;

    @Autowired
    UserRepo userRepo;

    /**
     * Opens (or resumes) an attempt and returns the quiz as the student should see it:
     * question text and options only, never the answer.
     */
    @Transactional
    public QuizStartDTO startQuiz(Integer quizId, String username) {
        Users student = userRepo.findByusername(username);
        if (student == null) {
            throw new NoSuchElementException("No such user: " + username);
        }

        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("No such quiz: " + quizId));

        QuizAttempt attempt = attemptRepo
                .findByStudentIdAndQuizIdAndCompletedAtIsNull(student.getId(), quizId)
                .orElseGet(() -> {
                    QuizAttempt fresh = new QuizAttempt();
                    fresh.setStudent(student);
                    fresh.setQuiz(quiz);
                    fresh.setStartedAt(LocalDateTime.now());
                    return attemptRepo.save(fresh);
                });

        QuizStartDTO dto = new QuizStartDTO();
        dto.setAttemptId(attempt.getId());
        dto.setTitle(quiz.getTitle());
        dto.setTimeLimitMinutes(quiz.getTimeLimitInMinutes());

        List<QuizStartDTO.QuestionDTO> questions = new ArrayList<>();
        for (Question question : questionRepo.findAllWithOptionsByQuizId(quizId)) {
            QuizStartDTO.QuestionDTO qd = new QuizStartDTO.QuestionDTO();
            qd.setQuestionId(question.getId());
            qd.setText(question.getQuestion_text());
            qd.setOptions(new ArrayList<>(question.getOptions()));
            questions.add(qd);
        }
        dto.setQuestions(questions);
        return dto;
    }

    /**
     * Grades the submitted answers, closes the attempt and returns the score.
     */
    @Transactional
    public Integer submitQuiz(QuizSubmissionDTO submission) {
        QuizAttempt attempt = attemptRepo.findById(submission.getAttemptId())
                .orElseThrow(() -> new NoSuchElementException("No such attempt: " + submission.getAttemptId()));

        // One statement for every submitted question instead of one lookup per answer.
        List<Integer> questionIds = submission.getAnswers().stream()
                .map(QuizSubmissionDTO.AnswerDTO::getQuestionId)
                .toList();
        Map<Integer, Question> questionsById = questionRepo.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        int score = 0;
        List<AttemptAnswer> graded = new ArrayList<>();
        for (QuizSubmissionDTO.AnswerDTO submitted : submission.getAnswers()) {
            Question question = questionsById.get(submitted.getQuestionId());
            if (question == null) {
                throw new NoSuchElementException("No such question: " + submitted.getQuestionId());
            }

            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(submitted.getSelectedOption());
            answer.setCorrect(question.getAnswer() != null
                    && question.getAnswer().equals(submitted.getSelectedOption()));
            if (answer.isCorrect()) {
                score++;
            }
            graded.add(answer);
        }

        if (attempt.getAnswers() == null) {
            attempt.setAnswers(graded);
        } else {
            attempt.getAnswers().clear();
            attempt.getAnswers().addAll(graded);
        }
        attempt.setScore(score);
        attempt.setCompletedAt(LocalDateTime.now());
        attemptRepo.save(attempt);

        return score;
    }

    /**
     * Returns a completed attempt with every answer, the question it belongs to and that
     * question's options.
     */
    @Transactional(readOnly = true)
    public AttemptResultDTO getAttempt(String attemptId) {
        QuizAttempt attempt = attemptRepo.findByIdWithAnswers(attemptId)
                .orElseThrow(() -> new NoSuchElementException("No such attempt: " + attemptId));

        // Second statement hydrates every question's options at once; the questions are
        // already managed, so the loop below reads them straight from the persistence context.
        List<Integer> questionIds = attempt.getAnswers().stream()
                .map(answer -> answer.getQuestion().getId())
                .toList();
        if (!questionIds.isEmpty()) {
            questionRepo.findAllWithOptionsByIdIn(questionIds);
        }

        AttemptResultDTO dto = new AttemptResultDTO();
        dto.setAttemptId(attempt.getId());
        dto.setQuizTitle(attempt.getQuiz().getTitle());
        dto.setStudentName(attempt.getStudent().getUsername());
        dto.setScore(attempt.getScore());

        List<AttemptResultDTO.AnswerDTO> answers = new ArrayList<>();
        for (AttemptAnswer answer : attempt.getAnswers()) {
            Question question = answer.getQuestion();
            AttemptResultDTO.AnswerDTO ad = new AttemptResultDTO.AnswerDTO();
            ad.setQuestionId(question.getId());
            ad.setQuestionText(question.getQuestion_text());
            ad.setOptions(new ArrayList<>(question.getOptions()));
            ad.setSelectedOption(answer.getSelectedOption());
            ad.setCorrect(answer.isCorrect());
            answers.add(ad);
        }
        dto.setAnswers(answers);
        return dto;
    }
}
