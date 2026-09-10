package com.nibin.quiz;

import com.nibin.quiz.DTO.QuizSubmissionDTO;
import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.QuizState;
import com.nibin.quiz.Model.Role;
import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.QuizAttemptRepo;
import com.nibin.quiz.Repository.QuizEntityRepo;
import com.nibin.quiz.Repository.UserRepo;
import com.nibin.quiz.Service.QuizAttemptService;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Measures the SQL statements issued by the quiz-fetch, submission and attempt-fetch paths.
 * Deliberately NOT @Transactional: each service call must run in its own transaction so it
 * gets a cold persistence context, the way a real HTTP request does.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(ProxyDataSourceConfig.class)
class QueryCountTest {

    // Override to prove the counts are flat in N: -Dquiz.test.questions=20
    private static final int QUESTION_COUNT = Integer.getInteger("quiz.test.questions", 5);
    private static final int OPTIONS_PER_QUESTION = 4;

    @Autowired
    QuizAttemptService attemptService;

    @Autowired
    QuizEntityRepo quizRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    QuizAttemptRepo attemptRepo;

    private Integer quizId;
    private String username;
    private final List<Integer> questionIds = new ArrayList<>();

    @BeforeEach
    void seed() {
        attemptRepo.deleteAll();
        quizRepo.deleteAll();
        userRepo.deleteAll();

        Users student = new Users();
        student.setUsername("student-" + System.nanoTime());
        student.setPassword("x");
        student.setRole(Role.STUDENT);
        userRepo.save(student);
        username = student.getUsername();

        Quiz quiz = new Quiz();
        quiz.setTitle("Physics 101");
        quiz.setDescription("Mechanics basics");
        quiz.setState(QuizState.PUBLISHED);
        quiz.setTimeLimitInMinutes(30);

        List<Question> questions = new ArrayList<>();
        for (int q = 0; q < QUESTION_COUNT; q++) {
            Question question = new Question();
            question.setQuestion_text("Question " + q);
            List<String> options = new ArrayList<>();
            for (int o = 0; o < OPTIONS_PER_QUESTION; o++) {
                options.add("Q" + q + "-option-" + o);
            }
            question.setOptions(options);
            question.setAnswer("Q" + q + "-option-0");
            question.setQuiz(quiz);
            questions.add(question);
        }
        quiz.setQuestions(questions);
        quizRepo.save(quiz);
        quizId = quiz.getId();

        questionIds.clear();
        for (Question question : questions) {
            questionIds.add(question.getId());
        }
    }

    @Test
    void quizFetchPath() {
        SQLStatementCountValidator.reset();
        attemptService.startQuiz(quizId, username);
        report("quiz-fetch (GET /student/quiz/{id}/start)");

        // user + quiz + open-attempt lookup + questions-with-options, flat in N.
        SQLStatementCountValidator.assertSelectCount(4);
        SQLStatementCountValidator.assertInsertCount(1);
    }

    @Test
    void submissionPath() {
        String attemptId = attemptService.startQuiz(quizId, username).getAttemptId();

        QuizSubmissionDTO submission = new QuizSubmissionDTO();
        submission.setAttemptId(attemptId);
        List<QuizSubmissionDTO.AnswerDTO> answers = new ArrayList<>();
        for (int q = 0; q < QUESTION_COUNT; q++) {
            QuizSubmissionDTO.AnswerDTO answer = new QuizSubmissionDTO.AnswerDTO();
            answer.setQuestionId(questionIdAt(q));
            answer.setSelectedOption("Q" + q + "-option-0");
            answers.add(answer);
        }
        submission.setAnswers(answers);

        SQLStatementCountValidator.reset();
        int score = attemptService.submitQuiz(submission);
        report("attempt-submission (POST /student/quiz/submit)");
        assertEquals(QUESTION_COUNT, score);

        // attempt + all submitted questions in one IN-list. Inserts scale with the number
        // of answers, which is unavoidable write volume, not an N+1.
        SQLStatementCountValidator.assertSelectCount(2);
        SQLStatementCountValidator.assertInsertCount(QUESTION_COUNT);
        SQLStatementCountValidator.assertUpdateCount(1);
    }

    @Test
    void attemptFetchPath() {
        String attemptId = attemptService.startQuiz(quizId, username).getAttemptId();

        QuizSubmissionDTO submission = new QuizSubmissionDTO();
        submission.setAttemptId(attemptId);
        List<QuizSubmissionDTO.AnswerDTO> answers = new ArrayList<>();
        for (int q = 0; q < QUESTION_COUNT; q++) {
            QuizSubmissionDTO.AnswerDTO answer = new QuizSubmissionDTO.AnswerDTO();
            answer.setQuestionId(questionIdAt(q));
            answer.setSelectedOption("Q" + q + "-option-0");
            answers.add(answer);
        }
        submission.setAnswers(answers);
        attemptService.submitQuiz(submission);

        SQLStatementCountValidator.reset();
        attemptService.getAttempt(attemptId);
        report("attempt-fetch (GET /student/attempt/{id})");

        // attempt+student+quiz+answers+questions in one, then options in one. Flat in N.
        SQLStatementCountValidator.assertSelectCount(2);
    }

    private Integer questionIdAt(int index) {
        return questionIds.get(index);
    }

    private void report(String label) {
        QueryCount count = QueryCountHolder.getGrandTotal();
        System.out.printf(
                "%n=== QUERY COUNT :: %s :: %d question(s) x %d option(s) ===%n"
                        + "  select=%d insert=%d update=%d delete=%d TOTAL=%d%n%n",
                label, QUESTION_COUNT, OPTIONS_PER_QUESTION,
                count.getSelect(), count.getInsert(), count.getUpdate(), count.getDelete(),
                count.getTotal());
    }
}
