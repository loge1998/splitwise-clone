package com.splitwise.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.splitwise.exceptions.BadRequestException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler({HttpClientErrorException.class})
  public ResponseEntity<String> handleClientError(HttpClientErrorException e) {
    logger.error(e.getStatusText() + ", status:" + e.getStatusCode(), e);
    return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
  }

  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<String> handleBadRequestError(RuntimeException e) {
    logger.error(e.getMessage(), e);
    return ResponseEntity.status(BAD_REQUEST).body(e.getMessage());
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<String> handleAllException(Exception e) {
    logger.error(e.getMessage(), e);
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(INTERNAL_SERVER_ERROR.getReasonPhrase());
  }
}
