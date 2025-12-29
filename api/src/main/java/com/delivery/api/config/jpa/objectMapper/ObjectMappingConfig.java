package com.delivery.api.config.jpa.objectMapper;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMappingConfig {

    @Bean
    public ObjectMapper objectMapper() {
        //커스텀으로 사용
        var objectMapper = new ObjectMapper();

        objectMapper.registerModule(new Jdk8Module());

        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);//모르는 json필드는 무시
        //필드가 없는 객체를 직렬화할때 오류없이 JSON으로처리
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 날짜 관련 직렬화 설정
        // Java 날짜 및 시간(LocalDate, LocalDateTime)을 타임스탬프 형식이 아닌 ISO-8601 문자열 형식으로 직렬화하도록 설정
        // 날짜와 시간이 타임스탬프가 아닌 문자열 형식으로 JSON에 기록되도록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // WRITE_DATES_AS_TIMESTAMPS : 기본이 false

        // 스네이크 케이스 -> API 응답 / 요청을 snake_case로 통일할 때 사용
        objectMapper.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());

        return objectMapper;
    }
}
