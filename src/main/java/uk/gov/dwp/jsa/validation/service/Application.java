package uk.gov.dwp.jsa.validation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import uk.gov.dwp.jsa.validation.service.config.ValidationServiceObjectMapperProvider;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(value = "uk.gov.dwp.jsa")
@EnableAsync(proxyTargetClass = true)
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        final ValidationServiceObjectMapperProvider objectMapperProvider = new ValidationServiceObjectMapperProvider();
        return objectMapperProvider.get();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


}
