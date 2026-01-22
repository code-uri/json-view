package com.monitorjbl.json.server;

import tools.jackson.databind.json.JsonMapper;
import com.monitorjbl.json.JsonViewSupportFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
@ComponentScan({"com.monitorjbl.json.server"})
public class Context implements WebMvcConfigurer {
  @Bean
  public JsonViewSupportFactoryBean views() {
    return new JsonViewSupportFactoryBean(JacksonConfiguration.configureJackson(JsonMapper.builder().build()), DefaultViewFactory.instance());
  }
}
