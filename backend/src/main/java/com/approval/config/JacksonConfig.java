package com.approval.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson 配置
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 Jackson ObjectMapper
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 设置时区
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            
            // 禁用将日期写为时间戳
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            
            // LocalDateTime 格式化
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            builder.serializers(new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializers(new LocalDateTimeDeserializer(dateTimeFormatter));
            
            // LocalDate 格式化
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            builder.serializers(new LocalDateSerializer(dateFormatter));
            builder.deserializers(new LocalDateDeserializer(dateFormatter));
        };
    }
}
