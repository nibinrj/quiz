package com.nibin.quiz;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;

import javax.sql.DataSource;

/**
 * Wraps the test DataSource in a datasource-proxy that counts statements, which is what
 * Hypersistence Utils' SQLStatementCountValidator reads from.
 */
@TestConfiguration
public class ProxyDataSourceConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource dataSource && !(bean instanceof ProxyDataSource)) {
            return ProxyDataSourceBuilder.create(dataSource)
                    .name("quiz-test-ds")
                    .countQuery()
                    .build();
        }
        return bean;
    }
}
