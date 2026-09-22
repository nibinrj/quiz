package com.nibin.quiz.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.QuizAttempt;
import com.nibin.quiz.Model.Role;
import com.nibin.quiz.Repository.QuizAttemptRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** B. Attempt lifecycle: create quiz, start, submit, score, and confirm it is persisted. */
class AttemptLifecycleIT extends AbstractPostgresIT {

    private static final int QUESTION_COUNT = 3;

    @Autowired
    QuizAttemptRepo attemptRepo;

    private String token;
    private Quiz quiz;

    @BeforeEach
    void signIn() throws Exception {
        createUser("dave", "s3cret-password", Role.STUDENT);
        token = login("dave", "s3cret-password");
        quiz = createQuiz("Mechanics", QUESTION_COUNT, 4);
    }

    @Test
    @DisplayName("all answers correct gives a full score and is persisted")
    void allCorrect() throws Exception {
        JsonNode started = startAttempt();
        String attemptId = started.path("attemptId").asText();

        assertEquals(QUESTION_COUNT, started.path("questions").size());
        assertEquals("Mechanics", started.path("title").asText());

        Map<Integer, String> answers = new LinkedHashMap<>();
        for (int q = 0; q < QUESTION_COUNT; q++) {
            answers.put(questionId(started, q), correctAnswerFor(q));
        }

        assertEquals(QUESTION_COUNT, submit(attemptId, answers));
        assertPersisted(attemptId, QUESTION_COUNT, QUESTION_COUNT);
    }

    @Test
    @DisplayName("one wrong answer is excluded from the score and stored as incorrect")
    void oneWrongAnswer() throws Exception {
        JsonNode started = startAttempt();
        String attemptId = started.path("attemptId").asText();

        Map<Integer, String> answers = new LinkedHashMap<>();
        answers.put(questionId(started, 0), correctAnswerFor(0));
        answers.put(questionId(started, 1), wrongAnswerFor(1));
        answers.put(questionId(started, 2), correctAnswerFor(2));

        assertEquals(2, submit(attemptId, answers));

        JsonNode result = fetchAttempt(attemptId);
        assertEquals(2, result.path("score").asInt());
        assertEquals(QUESTION_COUNT, result.path("answers").size());
        assertEquals(1, countIncorrect(result), "exactly one answer should be marked incorrect");

        JsonNode wrong = answerFor(result, questionId(started, 1));
        assertFalse(wrong.path("correct").asBoolean(), "the wrong answer must be flagged incorrect");
        assertEquals(wrongAnswerFor(1), wrong.path("selectedOption").asText());

        assertPersisted(attemptId, 2, QUESTION_COUNT);
    }

    @Test
    @DisplayName("a partial submission scores and stores only the answered questions")
    void partialSubmission() throws Exception {
        JsonNode started = startAttempt();
        String attemptId = started.path("attemptId").asText();

        Map<Integer, String> answers = new LinkedHashMap<>();
        answers.put(questionId(started, 0), correctAnswerFor(0));
        answers.put(questionId(started, 2), correctAnswerFor(2));

        assertEquals(2, submit(attemptId, answers));

        JsonNode result = fetchAttempt(attemptId);
        assertEquals(2, result.path("score").asInt());
        assertEquals(2, result.path("answers").size(), "unanswered questions must not be stored");
        assertEquals(0, countIncorrect(result));

        assertPersisted(attemptId, 2, 2);
    }

    private JsonNode startAttempt() throws Exception {
        String body = mockMvc.perform(get("/student/quiz/{id}/start", quiz.getId())
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private int submit(String attemptId, Map<Integer, String> answersByQuestionId) throws Exception {
        List<Map<String, Object>> answers = new ArrayList<>();
        answersByQuestionId.forEach((questionId, selected) ->
                answers.add(Map.of("questionId", questionId, "selectedOption", selected)));

        String payload = objectMapper.writeValueAsString(
                Map.of("attemptId", attemptId, "answers", answers));

        String body = mockMvc.perform(post("/student/quiz/submit")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).path("score").asInt();
    }

    private JsonNode fetchAttempt(String attemptId) throws Exception {
        String body = mockMvc.perform(get("/student/attempt/{id}", attemptId)
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    /** Reads straight from the database, bypassing the API, to prove it really was written. */
    private void assertPersisted(String attemptId, int expectedScore, int expectedAnswerRows) {
        QuizAttempt stored = attemptRepo.findByIdWithAnswers(attemptId).orElseThrow();
        assertEquals(expectedScore, stored.getScore());
        assertNotNull(stored.getCompletedAt(), "submitting must close the attempt");
        assertEquals(expectedAnswerRows, stored.getAnswers().size());

        Integer answerRows = jdbcTemplate.queryForObject(
                "select count(*) from attempt_answer where attempt_id = ?", Integer.class, attemptId);
        assertEquals(expectedAnswerRows, answerRows);
    }

    /**
     * The id of the question seeded at {@code index}, found by its text. Deliberately not by
     * position in the payload: the start endpoint does not order its questions, and
     * PostgreSQL is free to return them in any order.
     */
    private int questionId(JsonNode started, int index) {
        String text = quiz.getTitle() + " question " + index;
        for (JsonNode question : started.path("questions")) {
            if (text.equals(question.path("text").asText())) {
                return question.path("questionId").asInt();
            }
        }
        throw new AssertionError("start payload has no question with text '" + text + "'");
    }

    private static JsonNode answerFor(JsonNode result, int questionId) {
        for (JsonNode answer : result.path("answers")) {
            if (answer.path("questionId").asInt() == questionId) {
                return answer;
            }
        }
        throw new AssertionError("no stored answer for question " + questionId);
    }

    private static long countIncorrect(JsonNode result) {
        long incorrect = 0;
        for (JsonNode answer : result.path("answers")) {
            if (!answer.path("correct").asBoolean()) {
                incorrect++;
            }
        }
        return incorrect;
    }
}
