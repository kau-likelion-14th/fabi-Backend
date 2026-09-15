package likelion14th.lte.global.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SwaggerTests.TestApp.class, properties = {
        "jwt.secret=test-secret-with-at-least-32-bytes-long",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"
})
class SwaggerTests {
    @Autowired
    WebApplicationContext context;

    @Test
    void swaggerAndDocsLoadWithoutAuthentication() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/swagger-ui/index.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/api-docs/swagger-config")).andExpect(status().isOk())
                .andExpect(jsonPath("configUrl").value("/api-docs/swagger-config"));
        mvc.perform(get("/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("info.title").value("LTE API"));
        mvc.perform(get("/api-docs/All APIs")).andExpect(status().isOk());
        mvc.perform(get("/api/private")).andExpect(status().isUnauthorized());
    }

    @Configuration
    @EnableAutoConfiguration
    @Import({SecurityConfig.class, JwtConfig.class, SwaggerConfig.class})
    static class TestApp {}
}
