package com.kompu.api.infrastructure.config.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kompu.api.entity.user.exception.PasswordNotMatchException;
import com.kompu.api.entity.user.exception.UserNotFoundException;
import com.kompu.api.entity.userrole.exception.UserRoleNotFoundException;
import com.kompu.api.entity.usertoken.exception.UserTokenNotFoundException;
import com.kompu.api.entity.usertoken.exception.UserTokenRevokedException;
import com.kompu.api.infrastructure.config.web.response.WebHttpErrorResponse;
import com.kompu.api.infrastructure.config.web.response.WebHttpResponse;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalRestControllerAdvice {

	private static final String EXCEPTION_CAUGHT_MESSAGE = "Exception caught: ";

	@ExceptionHandler(BindException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleValidationError(BindException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = ex.getFieldErrors().stream()
				.map(violation -> new WebHttpErrorResponse(
						violation.getField(),
						violation.getDefaultMessage()))
				.toList();

		return ResponseEntity.badRequest().body(WebHttpResponse.badRequest(messages));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleUserNotFoundException(
			UserNotFoundException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(WebHttpResponse.notFound(messages));
	}

	@ExceptionHandler(PasswordNotMatchException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handlePasswordNotMatchException(
			PasswordNotMatchException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(WebHttpResponse.badRequest(messages));
	}

	@ExceptionHandler(UserTokenNotFoundException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleUserTokenNotFoundException(
			UserTokenNotFoundException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(WebHttpResponse.notFound(messages));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleGenericException(Exception ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(WebHttpResponse.internalServerError(messages));
	}

	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handlePersistenceException(
			PersistenceException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.internalServerError().body(WebHttpResponse.internalServerError(messages));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleDataIntegrityViolationException(
			DataIntegrityViolationException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.internalServerError().body(WebHttpResponse.internalServerError(messages));
	}

	@ExceptionHandler(InvalidDataAccessApiUsageException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleInvalidDataAccessApiUsageException(
			InvalidDataAccessApiUsageException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> message = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.internalServerError().body(WebHttpResponse.internalServerError(message));
	}

	@ExceptionHandler(UserTokenRevokedException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleUserTokenRevokedException(
			UserTokenRevokedException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> message = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(WebHttpResponse.unauthorized(message));
	}

	@ExceptionHandler(UserRoleNotFoundException.class)
	public ResponseEntity<WebHttpResponse<List<WebHttpErrorResponse>>> handleUserRoleNotFoundException(
			UserRoleNotFoundException ex) {
		log.error(EXCEPTION_CAUGHT_MESSAGE, ex);
		List<WebHttpErrorResponse> messages = List.of(new WebHttpErrorResponse(null, ex.getMessage()));
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(WebHttpResponse.notFound(messages));
	}

}
