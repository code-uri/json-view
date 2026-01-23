package com.monitorjbl.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * A {@link ResponseBodyAdvice} implementation that adds support for programmatic
 * JsonView configuration via {@link JsonResult}.
 *
 * <p>When {@link JsonResultRetriever#hasValue()} returns true, the JsonView object
 * is retrieved and used to wrap the response body. Additionally, if a default view
 * is configured, it will be applied when no programmatic view is set.
 *
 * @see JsonResult
 * @see JsonView
 * @see DefaultView
 */
public class ProgramaticJsonViewResponseBodyAdvice implements ResponseBodyAdvice<Object> {
  private static final Logger log = LoggerFactory.getLogger(ProgramaticJsonViewResponseBodyAdvice.class);

  private final DefaultView defaultView;

  public ProgramaticJsonViewResponseBodyAdvice(DefaultView defaultView) {
    this.defaultView = defaultView;
  }

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    // Only apply advice for Jackson converters
    return AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {
    
    // Handle ResponseEntity wrapper
    Object actualBody = body;
    if (body instanceof ResponseEntity) {
      actualBody = ((ResponseEntity<?>) body).getBody();
    }

    Object result = actualBody;
    
    // Check if a programmatic JsonView was set
    if (JsonResultRetriever.hasValue()) {
      JsonView jsonView = JsonResultRetriever.retrieve();
      log.debug("Found programmatic JsonView for [{}]", jsonView.getValue().getClass());
      result = jsonView;
    } else {
      // Try to apply default view
      JsonView view = defaultView.getMatch(actualBody);
      if (view != null) {
        log.debug("Default view found for {}, applied before serialization", 
                  actualBody != null ? actualBody.getClass().getCanonicalName() : "null");
        result = view;
      } else {
        log.debug("No JsonView found, using returned value");
      }
    }

    // If body was a ResponseEntity, we need to preserve that wrapper
    if (body instanceof ResponseEntity) {
      ResponseEntity<?> responseEntity = (ResponseEntity<?>) body;
      return ResponseEntity.status(responseEntity.getStatusCode())
          .headers(responseEntity.getHeaders())
          .body(result);
    }

    return result;
  }
}
