package com.nibin.quiz.it;

import com.nibin.quiz.Model.Quiz;
import com.nibin.quiz.Model.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A. Auth flow. */
class AuthFlowIT extends AbstractPostgresIT {

    private static final String PROTECTED_ENDPOINT = "/student/attempt/does-not-matter";

    private static final String NEW_QUESTION =
            "{\"question_text\":\"2 + 2?\",\"options\":[\"3\",\"4\"],\"answer\":\"4\"}";

    @Value("${jwt.secret}")
    String jwtSecret;

    @Test
    @DisplayName("successful login returns a usable JWT")
    void loginReturnsJwt() throws Exception {
        createUser("alice", "s3cret-password", Role.STUDENT);

        String token = login("alice", "s3cret-password");

        assertFalse(token.isBlank(), "login should return a token");
        assertEquals(3, token.split("\\.").length, "token should have header.payload.signature");

        String subject = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        assertEquals("alice", subject);

        // and it actually authenticates: a protected endpoint serves the request
        Quiz quiz = createQuiz("Auth check", 1, 2);
        mockMvc.perform(get("/student/quiz/{id}/start", quiz.getId())
                        .header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("protected endpoint without a token returns 401")
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get(PROTECTED_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("protected endpoint with a malformed token returns 401")
    void malformedTokenReturns401() throws Exception {
        mockMvc.perform(get(PROTECTED_ENDPOINT).header(AUTHORIZATION, bearer("this-is-not-a-jwt")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("protected endpoint with an expired token returns 401")
    void expiredTokenReturns401() throws Exception {
        createUser("bob", "s3cret-password", Role.STUDENT);

        mockMvc.perform(get(PROTECTED_ENDPOINT).header(AUTHORIZATION, bearer(expiredTokenFor("bob"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("STUDENT token against an ADMIN-only path returns 403")
    void studentTokenOnAdminPathReturns403() throws Exception {
        createUser("carol", "s3cret-password", Role.STUDENT);
        String token = login("carol", "s3cret-password");

        // NOTE: no controller maps anything under /admin/**. This asserts the authorization
        // rule in securityconfig (`/admin/** -> hasRole("ADMIN")`), which rejects the request
        // before dispatch, not a real admin handler.
        mockMvc.perform(get("/admin/anything").header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("validly signed token for a user that no longer exists returns 401")
    void tokenForDeletedUserReturns401() throws Exception {
        String token = Jwts.builder()
                .subject("ghost")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()))
                .signWith(signingKey())
                .compact();

        mockMvc.perform(get(PROTECTED_ENDPOINT).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("STUDENT cannot create or delete questions")
    void studentCannotManageQuestions() throws Exception {
        createUser("frank", "s3cret-password", Role.STUDENT);
        String token = login("frank", "s3cret-password");

        mockMvc.perform(post("/add").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content(NEW_QUESTION))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/addMany").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content("[" + NEW_QUESTION + "]"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/delete/{id}", 1).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("STAFF can create questions")
    void staffCanCreateQuestions() throws Exception {
        createUser("grace", "s3cret-password", Role.STAFF);
        String token = login("grace", "s3cret-password");

        mockMvc.perform(post("/add").header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON).content(NEW_QUESTION))
                .andExpect(status().isOk());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String expiredTokenFor(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(now - Duration.ofHours(2).toMillis()))
                .expiration(new Date(now - Duration.ofHours(1).toMillis()))
                .signWith(signingKey())
                .compact();
    }
}
