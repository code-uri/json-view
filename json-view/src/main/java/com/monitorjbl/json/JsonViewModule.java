package com.monitorjbl.json;

import tools.jackson.core.Version;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

public class JsonViewModule extends SimpleModule {

  private final JsonViewSerializer jsonView;

  public JsonViewModule() {
    this(new JsonViewSerializer());
  }

  public JsonViewModule(JsonViewSerializer jsonView) {
    super(new Version(0, 16, 0, "", "com.monitorjbl", "json-view"));
    addSerializer(JsonView.class, jsonView);
    this.jsonView = jsonView;
  }

  public JsonViewModule withDefaultMatcherBehavior(MatcherBehavior matcherBehavior){
    this.jsonView.setDefaultMatcherBehavior(matcherBehavior);
    return this;
  }

  public <E> JsonViewModule registerSerializer(Class<E> cls, ValueSerializer<E> serializer) {
    jsonView.registerCustomSerializer(cls, serializer);
    return this;
  }

}
