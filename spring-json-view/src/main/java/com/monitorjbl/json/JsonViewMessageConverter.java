package com.monitorjbl.json;

import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import java.io.IOException;

public class JsonViewMessageConverter extends JacksonJsonHttpMessageConverter {

  private JsonViewSerializer serializer = new JsonViewSerializer();

  public JsonViewMessageConverter() {
    super(createMapper(new JsonViewSerializer()));
  }

  public JsonViewMessageConverter(JsonMapper mapper) {
    super(addModuleToMapper(mapper, new JsonViewSerializer()));
  }

  private static JsonMapper createMapper(JsonViewSerializer serializer) {
    SimpleModule module = new SimpleModule();
    module.<JsonView>addSerializer(JsonView.class, serializer);
    return JsonMapper.builder()
        .addModule(module)
        .build();
  }

  private static JsonMapper addModuleToMapper(JsonMapper mapper, JsonViewSerializer serializer) {
    SimpleModule module = new SimpleModule();
    module.<JsonView>addSerializer(JsonView.class, serializer);
    return mapper.rebuild()
        .addModule(module)
        .build();
  }

  /**
   * Registering custom serializer allows to the JSonView to deal with custom serializations for certains field types.<br>
   * This way you could register for instance a JODA serialization as  a DateTimeSerializer. <br>
   * Thus, when JSonView find a field of that type (DateTime), it will delegate the serialization to the serializer specified.<br>
   * Example:<br>
   * <code>
   * JsonViewSupportFactoryBean bean = new JsonViewSupportFactoryBean( mapper );
   * bean.registerCustomSerializer( DateTime.class, new DateTimeSerializer() );
   * </code>
   *
   * @param <T>     Type class of the serializer
   * @param class1  {@link Class} the class type you want to add a custom serializer
   * @param forType {@link ValueSerializer} the serializer you want to apply for that type
   */
  public <T> void registerCustomSerializer(Class<T> class1, ValueSerializer<T> forType) {
    this.serializer.registerCustomSerializer(class1, forType);
  }

  /**
   * Unregister a previously registtered serializer. @see registerCustomSerializer
   *
   * @param <T>    Type class of the serializer
   * @param class1 {@link Class} the class type for which you want to remove a custom serializer
   */
  public <T> void unregisterCustomSerializer(Class<T> class1) {
    this.serializer.unregisterCustomSerializer(class1);
  }

  @Override
  protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    super.writeInternal(object, outputMessage);
  }

}