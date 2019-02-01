
package okbem.br31.server.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        Module javaTimeModule = new JavaTimeModule()
            .addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME)
            )
            .addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME)
            )
            ;

        return new ObjectMapper()
            .registerModule(javaTimeModule)

            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            ;
    }

}

