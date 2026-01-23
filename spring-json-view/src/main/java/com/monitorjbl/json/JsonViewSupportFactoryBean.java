package com.monitorjbl.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring configuration for JsonView support using custom return value handlers and ResponseBodyAdvice.
 * 
 * <p>This configuration implements {@link WebMvcConfigurer} to customize message converters
 * and uses a {@link BeanPostProcessor} to replace Spring's default return value handlers
 * with JsonView-aware versions that check ThreadLocal for programmatic JsonView configuration.
 * 
 * <p>The implementation supports:
 * <ul>
 *   <li>Void controller methods that use json.use() without returning a value</li>
 *   <li>Non-void methods that use json.use().returnValue() or default views</li>
 *   <li>ResponseEntity returns with JsonView transformations</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * &#64;Configuration
 * public class MyConfig {
 *   &#64;Bean
 *   public JsonViewSupportFactoryBean jsonViewSupport() {
 *     return new JsonViewSupportFactoryBean(mapper, defaultView);
 *   }
 * }
 * </pre>
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JsonViewSupportFactoryBean implements WebMvcConfigurer {
  protected static final Logger log = LoggerFactory.getLogger(JsonViewSupportFactoryBean.class);

  protected final JsonViewMessageConverter converter;
  protected final DefaultView defaultView;

  public JsonViewSupportFactoryBean() {
    this(JsonMapper.builder().build());
  }

  public JsonViewSupportFactoryBean(JsonMapper mapper) {
    this(new JsonViewMessageConverter(mapper), DefaultView.create());
  }

  public JsonViewSupportFactoryBean(DefaultView defaultView) {
    this(new JsonViewMessageConverter(JsonMapper.builder().build()), defaultView);
  }

  public JsonViewSupportFactoryBean(JsonMapper mapper, DefaultView defaultView) {
    this(new JsonViewMessageConverter(mapper), defaultView);
  }

  private JsonViewSupportFactoryBean(JsonViewMessageConverter converter, DefaultView defaultView) {
    this.converter = converter;
    this.defaultView = defaultView;
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // Remove existing Jackson converters to avoid conflicts
    removeJacksonConverters(converters);
    
    // Add our custom JsonViewMessageConverter
    converters.add(converter);
    
    log.debug("Added JsonViewMessageConverter to the converter list");
  }

  /**
   * BeanPostProcessor to replace Spring's default return value handlers with JsonView-aware versions.
   * This is executed after the RequestMappingHandlerAdapter bean is created but before it's fully initialized.
   */
  @Bean
  public BeanPostProcessor jsonViewHandlerReplacer() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
          RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
          
          // Get the current message converters and add ours
          List<HttpMessageConverter<?>> converters = new ArrayList<>(adapter.getMessageConverters());
          removeJacksonConverters(converters);
          converters.add(converter);
          adapter.setMessageConverters(converters);
          
          log.debug("Configured message converters on RequestMappingHandlerAdapter");
        }
        return bean;
      }
      
      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerAdapter) {
          RequestMappingHandlerAdapter adapter = (RequestMappingHandlerAdapter) bean;
          
          // Get current handlers and replace specific ones
          List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(adapter.getReturnValueHandlers());
          decorateHandlers(handlers);
          adapter.setReturnValueHandlers(handlers);
          
          log.debug("Replaced return value handlers with JsonView-aware versions");
        }
        return bean;
      }
    };
  }

  /**
   * Replace specific return value handlers with JsonView-aware versions.
   * This mimics the original implementation but is done via BeanPostProcessor.
   */
  protected void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {
    List<HttpMessageConverter<?>> converters = new ArrayList<>();
    converters.add(converter);
    
    for (int i = 0; i < handlers.size(); i++) {
      HandlerMethodReturnValueHandler handler = handlers.get(i);
      if (handler instanceof ResponseEntityReturnValueHandler) {
        handlers.set(i, new JsonViewHttpEntityMethodProcessor(converters));
        log.debug("Replaced ResponseEntityReturnValueHandler with JsonViewHttpEntityMethodProcessor");
      } else if (handler instanceof RequestResponseBodyMethodProcessor) {
        handlers.set(i, new JsonViewReturnValueHandler(converters, defaultView));
        log.debug("Replaced RequestResponseBodyMethodProcessor with JsonViewReturnValueHandler");
        break; // Only replace the first one
      }
    }
  }

  /**
   * Register the ResponseBodyAdvice that intercepts response bodies
   * and applies JsonView transformations for additional scenarios.
   */
  @Bean
  public ResponseBodyAdvice<Object> programaticJsonViewResponseBodyAdvice() {
    return new ProgramaticJsonViewResponseBodyAdvice(defaultView);
  }

  protected void removeJacksonConverters(List<HttpMessageConverter<?>> converters) {
    Iterator<HttpMessageConverter<?>> iter = converters.iterator();
    while(iter.hasNext()) {
      HttpMessageConverter<?> next = iter.next();
      // Remove both Jackson 2.x (MappingJackson2) and Jackson 3.x (JacksonJson) converters
      if (next.getClass().getSimpleName().startsWith("MappingJackson2") 
          || next.getClass().getSimpleName().startsWith("JacksonJson")) {
        log.debug("Removing {} as it interferes with us", next.getClass().getName());
        iter.remove();
      }
    }
  }

  /**
   * Registering custom serializer allows to the JSonView to deal with custom serializations for certains field types.<br>
   * This way you could register for instance a JODA serialization as  a DateTimeSerializer. <br>
   * Thus, when JSonView find a field of that type (DateTime), it will delegate the serialization to the serializer specified.<br>
   * Example:<br>
   * <code>
   *   JsonViewSupportFactoryBean bean = new JsonViewSupportFactoryBean( mapper );
   *   bean.registerCustomSerializer( DateTime.class, new DateTimeSerializer() );
   * </code>
   * @param <T> Type class of the serializer
   * @param cls {@link Class} the class type you want to add a custom serializer
   * @param forType {@link ValueSerializer} the serializer you want to apply for that type
   */
  public <T> void registerCustomSerializer( Class<T> cls, ValueSerializer<T> forType )
  {
      this.converter.registerCustomSerializer( cls, forType );
  }
  
  
  /**
   * Unregister a previously registtered serializer. @see registerCustomSerializer
   * @param cls The class type the serializer was registered for
   */
  public void unregisterCustomSerializer( Class<?> cls )
  {
      this.converter.unregisterCustomSerializer(cls);
  }

}
