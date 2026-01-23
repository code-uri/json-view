package com.monitorjbl.json.server;

import tools.jackson.databind.json.JsonMapper;
import com.monitorjbl.json.JsonViewSupportFactoryBean;
import com.monitorjbl.json.ProgramaticJsonViewResponseBodyAdvice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.List;

@EnableWebMvc
@Configuration
@ComponentScan({"com.monitorjbl.json.server"})
public class Context implements WebMvcConfigurer {
  
  private JsonViewSupportFactoryBean cachedViews;

  @Bean
  public JsonViewSupportFactoryBean views() {
    if (cachedViews == null) {
      cachedViews = new JsonViewSupportFactoryBean(
        JacksonConfiguration.configureJackson(JsonMapper.builder().build()), 
        DefaultViewFactory.instance()
      );
    }
    return cachedViews;
  }

  @Bean
  public ResponseBodyAdvice<Object> programaticJsonViewResponseBodyAdvice() {
    return new ProgramaticJsonViewResponseBodyAdvice(DefaultViewFactory.instance());
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    JsonViewSupportFactoryBean bean = views();
    // Remove existing Jackson converters to avoid conflicts
    JsonViewSupportFactoryBean.removeJacksonConverters(converters);
    
    // Add our custom JsonViewMessageConverter
    converters.add(bean.getConverter());
  }
}
