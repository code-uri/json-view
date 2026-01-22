package com.monitorjbl.json.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Configures Jackson mapper for unit tests. Works with XML-based configuration
 * if it is included as a bean.
 */
public class JacksonConfiguration {
  private JsonMapper mapper;

  public JacksonConfiguration(JsonMapper mapper) {
    this.mapper = configureJackson(mapper);
  }

  public static JsonMapper configureJackson(JsonMapper mapper) {
    mapper = mapper.rebuild()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .changeDefaultPropertyInclusion(
            incl -> JsonInclude.Value.construct(Include.NON_NULL, Include.NON_NULL))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    return mapper;
  }
}
