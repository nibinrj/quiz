package com.nibin.quiz.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibin.quiz.Model.Question;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.QuizState;
import com.nibin.quiz.Model.Role;
import com.nibin.quiz.Model.Users;
import com.nibin.quiz.Repository.QuizEntityRepo;
import com.nibin.quiz.Repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base for every integration test: one real PostgreSQL, shared by all of them.
 *
 * The container is started once in the static initialiser below and deliberately never
 * stopped - Testcontainers' Ryuk sidecar removes it when the JVM exits. This is the
 * documented singleton-container pattern, and it is why the container is NOT declared with
 * @Container/@Testcontainers: that combination stops a static container in afterAll, which
 * would tear the database down between test classes.
 *
 * The schema is created by Hibernate with ddl-auto=update, exactly as in production, so the
 * tests exercise the same DDL path the real application uses. Because the database outlives
 * each test, every table is truncated before each one rather than being recreated.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
public abstract class AbstractPostgresIT {

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepo userRepo;

    @Autowired
    protected QuizEntityRepo quizRepo;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    /**
     * Empties every table in the public schema. Discovering the tables from pg_tables rather
     * than hard-coding them keeps this correct as the entity model grows.
     */
    @BeforeEach
    void resetDatabase() {
        List<String> tables = jdbcTemplate.queryForList(
                "select tablename from pg_tables where schemaname = 'public'", String.class);
        if (!tables.isEmpty()) {
            jdbcTemplate.execute("truncate table " + String.join(", ", tables)
                    + " restart identity cascade");
        }
    }

    protected Users createUser(String username, String rawPassword, Role role) {
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepo.save(user);
    }

    /**
     * Seeds a quiz whose question i has options "qI-optionO" and correct answer "qI-option0".
     */
    protected Quiz createQuiz(String title, int questionCount, int optionsPerQuestion) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(title + " description");
        quiz.setState(QuizState.PUBLISHED);
        quiz.setTimeLimitInMinutes(30);

        List<Question> questions = new ArrayList<>();
        for (int q = 0; q < questionCount; q++) {
            Question question = new Question();
            question.setQuestion_text(title + " question " + q);
            List<String> options = new ArrayList<>();
            for (int o = 0; o < optionsPerQuestion; o++) {
                options.add("q" + q + "-option" + o);
            }
            question.setOptions(options);
            question.setAnswer(correctAnswerFor(q));
            question.setQuiz(quiz);
            questions.add(question);
        }
        quiz.setQuestions(questions);
        return quizRepo.save(quiz);
    }

    protected static String correctAnswerFor(int questionIndex) {
        return "q" + questionIndex + "-option0";
    }

    protected static String wrongAnswerFor(int questionIndex) {
        return "q" + questionIndex + "-option1";
    }

    /** Logs in over HTTP and returns the raw JWT. */
    protected String login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("username", username, "password", password));

        String response = mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.path("token").asText();
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
