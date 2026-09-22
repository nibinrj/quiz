package com.nibin.quiz.it;

import com.nibin.quiz.ProxyDataSourceConfig;
import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.Role;
import io.hypersistence.utils.jdbc.validator.SQLStatementCountValidator;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C. N+1 regression guard for the quiz-fetch endpoint, against real PostgreSQL.
 *
 * Rather than pinning an exact number - which would break on any unrelated query change -
 * this serves the same endpoint for a 3-question quiz and a 30-question quiz and requires
 * the select count to be identical. That is precisely the property the JOIN FETCH fix buys:
 * the statement count is independent of the number of questions and options. If someone
 * reintroduces a per-question or per-option query, the two counts diverge and this fails.
 */
@Import(ProxyDataSourceConfig.class)
class QuizFetchQueryCountIT extends AbstractPostgresIT {

    private static final int SMALL = 3;
    private static final int LARGE = 30;
    private static final int OPTIONS_PER_QUESTION = 4;

    /**
     * Selects the endpoint needs, none of which depend on the question count:
     * user (JwtFilter), user again (QuizAttemptService.startQuiz), quiz, open-attempt lookup,
     * questions+options. The duplicate user lookup is a redundancy, not an N+1.
     */
    private static final int EXPECTED_SELECTS = 5;

    @Test
    @DisplayName("quiz-fetch statement count does not grow with the number of questions")
    void quizFetchIsFlatInQuestionCount() throws Exception {
        createUser("erin", "s3cret-password", Role.STUDENT);
        String token = login("erin", "s3cret-password");

        Quiz smallQuiz = createQuiz("Small", SMALL, OPTIONS_PER_QUESTION);
        Quiz largeQuiz = createQuiz("Large", LARGE, OPTIONS_PER_QUESTION);

        long smallSelects = selectsForStart(smallQuiz, token);
        long largeSelects = selectsForStart(largeQuiz, token);

        System.out.printf("%n=== quiz-fetch selects :: %d questions = %d :: %d questions = %d ===%n%n",
                SMALL, smallSelects, LARGE, largeSelects);

        assertEquals(smallSelects, largeSelects,
                "select count grew from " + SMALL + " to " + LARGE + " questions ("
                        + smallSelects + " -> " + largeSelects + "): the N+1 is back");

        // Absolute ceiling, so a constant-factor regression cannot hide behind equality.
        assertTrue(largeSelects <= EXPECTED_SELECTS,
                "expected at most " + EXPECTED_SELECTS + " selects, got " + largeSelects);
    }

    private long selectsForStart(Quiz quiz, String token) throws Exception {
        SQLStatementCountValidator.reset();
        mockMvc.perform(get("/student/quiz/{id}/start", quiz.getId())
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
        return QueryCountHolder.getGrandTotal().getSelect();
    }
}
