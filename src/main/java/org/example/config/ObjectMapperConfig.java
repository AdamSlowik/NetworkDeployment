package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import inet.ipaddr.mac.MACAddress;
import org.example.config.converters.MacDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MACAddress.class, new MacDeserializer());

        return JsonMapper.builder()
                .build()
                .findAndRegisterModules()
                .registerModule(module);
    }
}
