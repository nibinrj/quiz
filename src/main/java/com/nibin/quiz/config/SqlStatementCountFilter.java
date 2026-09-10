package com.nibin.quiz.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs how many JDBC statements Hibernate prepared while serving each request.
 * Dev profile only - Hibernate's statistics counters are SessionFactory-wide, so
 * overlapping requests will bleed into each other's totals. That is fine for spotting
 * N+1s by hand; the precise numbers come from the SQLStatementCountValidator tests.
 */
@Component
@Profile("dev")
@Slf4j
public class SqlStatementCountFilter extends OncePerRequestFilter {

    private final Statistics statistics;

    public SqlStatementCountFilter(EntityManagerFactory entityManagerFactory) {
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        this.statistics.setStatisticsEnabled(true);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long before = statistics.getPrepareStatementCount();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long statements = statistics.getPrepareStatementCount() - before;
            log.info("[sql-count] {} {} -> {} statement(s)",
                    request.getMethod(), request.getRequestURI(), statements);
        }
    }
}
