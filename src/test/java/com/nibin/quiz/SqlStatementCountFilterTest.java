package com.nibin.quiz;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.QuizState;
import com.nibin.quiz.Model.Role;
import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.QuizAttemptRepo;
import com.nibin.quiz.Repository.QuizEntityRepo;
import com.nibin.quiz.Repository.UserRepo;
import com.nibin.quiz.config.SqlStatementCountFilter;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the dev-profile counter is actually wired into the servlet chain and reports a
 * real number, rather than just being a bean that compiles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev"})
class SqlStatementCountFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    QuizEntityRepo quizRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    QuizAttemptRepo attemptRepo;

    @Test
    void logsStatementCountPerRequest() throws Exception {
        attemptRepo.deleteAll();
        quizRepo.deleteAll();
        userRepo.deleteAll();

        Users student = new Users();
        student.setUsername("filter-student");
        student.setPassword("x");
        student.setRole(Role.STUDENT);
        userRepo.save(student);

        Quiz quiz = new Quiz();
        quiz.setTitle("Filter quiz");
        quiz.setState(QuizState.PUBLISHED);
        quiz.setTimeLimitInMinutes(10);
        Question question = new Question();
        question.setQuestion_text("Q0");
        question.setOptions(new ArrayList<>(List.of("a", "b")));
        question.setAnswer("a");
        question.setQuiz(quiz);
        quiz.setQuestions(new ArrayList<>(List.of(question)));
        quizRepo.save(quiz);

        Logger logger = (Logger) LoggerFactory.getLogger(SqlStatementCountFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        try {
            mockMvc.perform(get("/student/quiz/{id}/start", quiz.getId())
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                    .user("filter-student").roles("STUDENT")))
                    .andExpect(status().isOk());
        } finally {
            logger.detachAppender(appender);
        }

        List<String> lines = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.startsWith("[sql-count]"))
                .toList();

        System.out.println("=== DEV FILTER OUTPUT ===");
        lines.forEach(System.out::println);

        assertTrue(lines.stream().anyMatch(line ->
                        line.contains("/student/quiz/") && !line.contains("-> 0 statement(s)")),
                "expected a non-zero per-request statement count, got: " + lines);
    }
}
