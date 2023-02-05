package com.splitwise.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonMessageParser {

  private static final Logger logger = LoggerFactory.getLogger(JsonMessageParser.class);
  private final ObjectMapper mapper;

  public JsonMessageParser(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public <T> Try<T> readValue(String input, TypeReference<T> type) {
    return Try.of(() -> mapper.readValue(input, type))
      .onFailure(exception -> logger.error(
        "Failed to parse the message, to class: " + type.getType().getTypeName(),
        exception));
  }

  public Try<String> toJson(Object value) {
    return Try.of(() -> mapper.writeValueAsString(value))
      .onFailure(exception -> logger.error(
        "Failed to generate json: from class" + value.getClass().getTypeName(),
        exception));
  }
}
