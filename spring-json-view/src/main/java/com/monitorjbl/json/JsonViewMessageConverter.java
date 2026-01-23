package com.monitorjbl.json;

import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

/**
 * HTTP message converter that uses Jackson 3.x for JSON serialization/deserialization
 * with support for JsonView objects.
 * 
 * <p>This converter delegates to a pre-configured JsonMapper that already has
 * the JsonView serializer registered.
 */
public class JsonViewMessageConverter extends AbstractHttpMessageConverter<Object> {

  private final JsonMapper mapper;
  private JsonViewSerializer serializer;

  public JsonViewMessageConverter() {
    super(MediaType.APPLICATION_JSON);
    this.serializer = new JsonViewSerializer();
    this.mapper = buildMapper(serializer);
  }

  public JsonViewMessageConverter(JsonMapper mapper) {
    super(MediaType.APPLICATION_JSON);
    this.mapper = addSerializerToMapper(mapper);
    this.serializer = new JsonViewSerializer();
  }

  /**
   * Builds a mapper with the JsonView serializer already configured.
   */
  private static JsonMapper buildMapper(JsonViewSerializer serializer) {
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, serializer);
    return JsonMapper.builder()
        .addModule(module)
        .build();
  }

  /**
   * Adds the JsonView serializer to an existing mapper.
   */
  private static JsonMapper addSerializerToMapper(JsonMapper mapper) {
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonView.class, new JsonViewSerializer());
    return mapper.rebuild()
        .addModule(module)
        .build();
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    // This converter handles any object
    return true;
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    try {
      return mapper.readValue(inputMessage.getBody(), clazz);
    } catch (Exception ex) {
      throw new HttpMessageNotReadableException("Could not read JSON", ex, inputMessage);
    }
  }

  @Override
  protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    try {
      outputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON);
      byte[] bytes = mapper.writeValueAsBytes(object);
      outputMessage.getBody().write(bytes);
      outputMessage.getBody().flush();
    } catch (Exception ex) {
      throw new HttpMessageNotWritableException("Could not write JSON", ex);
    }
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

}
