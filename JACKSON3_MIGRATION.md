# Jackson 3.x Migration Notes

## Overview

This project has been upgraded to use Jackson 3.x. This upgrade includes significant breaking changes due to Jackson 3.x's new package structure and API changes.

## Key Changes

### 1. Package Name Changes
- **Jackson Core/Databind**: `com.fasterxml.jackson` → `tools.jackson`
- **Jackson Annotations**: Remains `com.fasterxml.jackson.annotation` (uses 2.x version)

### 2. API Renames
- `JsonSerializer` → `ValueSerializer`
- `SerializerProvider` → `SerializationContext`
- `writeFieldName()` → `writeName()`
- `defaultSerializeValue()` → `writePOJO()`

### 3. Exception Handling
- All Jackson exceptions are now unchecked (RuntimeException)
- `IOException` no longer thrown from serialize methods

### 4. Deprecated API Removals
- `JsonSerialize.include()` removed (use `@JsonInclude` instead)
- `ObjectMapper.copy()` removed (use `rebuild()` instead)

## Spring Integration Requirements

### Spring Version Requirement

**The `spring-json-view` module requires Spring Framework 7.0+** to work with Jackson 3.x.

Spring Framework 7.0+ added Jackson 3.x support via the new `JacksonJsonHttpMessageConverter` class. Earlier versions (Spring 4.x-6.x) only support Jackson 2.x through `MappingJackson2HttpMessageConverter`.

This project has been upgraded to use Spring 7.0.3.

### Migration Path for Spring Integration

If you're using the `spring-json-view` module, you must:

1. **Upgrade to Spring Framework 7.0 or later**
   ```xml
   <dependency>
     <groupId>org.springframework</groupId>
     <artifactId>spring-webmvc</artifactId>
     <version>7.0.3</version>
   </dependency>
   ```

2. **Upgrade to Jakarta EE 9+** (Spring 7.x uses jakarta.servlet)
   ```xml
   <dependency>
     <groupId>jakarta.servlet</groupId>
     <artifactId>jakarta.servlet-api</artifactId>
     <version>6.0.0</version>
   </dependency>
   ```

3. **Use Java 17 or later** (required by Jackson 3.x and Spring 7.x)

### Alternative: Use json-view Module Only

If you cannot upgrade Spring, you can use the `json-view` module independently without Spring integration:

```xml
<dependency>
  <groupId>com.monitorjbl</groupId>
  <artifactId>json-view</artifactId>
  <version>1.1.0</version>
</dependency>
```

## Dependencies

### Jackson 3.x Dependencies

```xml
<properties>
  <jackson.version>3.0.4</jackson.version>
  <jackson.annotations.version>2.21</jackson.annotations.version>
</properties>

<dependencies>
  <!-- Jackson Core -->
  <dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>${jackson.version}</version>
  </dependency>
  
  <!-- Jackson Databind -->
  <dependency>
    <groupId>tools.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>${jackson.version}</version>
  </dependency>
  
  <!-- Jackson Annotations (still 2.x) -->
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>${jackson.annotations.version}</version>
  </dependency>
</dependencies>
```

### Java 8 Modules

Jackson 3.x includes built-in support for:
- `java.util.Optional` (formerly `jackson-datatype-jdk8`)
- `java.time` types (formerly `jackson-datatype-jsr310`)
- Parameter names (formerly `jackson-module-parameter-names`)

These modules are no longer needed as separate dependencies.

## Testing

The `json-view` module compiles and works with Jackson 3.x. The `spring-json-view` module requires Spring 6.x+ for runtime compatibility.

## References

- [Jackson 3.0 Migration Guide](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- [Jackson 3.0 Release Notes](https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0)
- [Spring Jackson 3 Support Announcement](https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring)
