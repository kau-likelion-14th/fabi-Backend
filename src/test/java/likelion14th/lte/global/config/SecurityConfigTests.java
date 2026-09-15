package likelion14th.lte.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTests {
    @Test
    void createsSecurityFilterChainWithBearerAuthentication() {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "jwt.secret=test-secret-with-at-least-32-bytes-long");
            context.register(WebSecurity.class, SecurityConfig.class, JwtConfig.class);
            context.refresh();

            var chain = context.getBean("filterChain", SecurityFilterChain.class);
            assertTrue(chain.getFilters().stream()
                    .anyMatch(BearerTokenAuthenticationFilter.class::isInstance));
        }
    }

    @Configuration
    @EnableWebSecurity
    static class WebSecurity {}
}
