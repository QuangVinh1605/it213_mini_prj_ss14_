package com.example.mini_project_ss14;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:smarthub_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.ai.openai.api-key=test-openai-key",
        "otel.sdk.disabled=true"
})
class MiniProjectSs14ApplicationTests {

    @Test
    void contextLoads() {
    }

}
