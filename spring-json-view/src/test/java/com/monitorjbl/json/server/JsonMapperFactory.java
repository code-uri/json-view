package com.monitorjbl.json.server;

import tools.jackson.databind.json.JsonMapper;

public class JsonMapperFactory {
  public static JsonMapper createJsonMapper() {
    return JsonMapper.builder().build();
  }
}
