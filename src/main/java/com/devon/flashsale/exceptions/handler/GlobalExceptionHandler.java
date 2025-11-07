package com.devon.flashsale.exceptions.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.devon.flashsale.dto.ErrorResponseDto;
import com.devon.flashsale.exceptions.FlashSaleAppException;
import com.devon.flashsale.exceptions.InvalidStateException;
import com.devon.flashsale.exceptions.OutOfStockException;
import com.devon.flashsale.exceptions.ResourceNotFoundException;
import com.devon.flashsale.exceptions.SaleWindowClosedException;
import com.devon.flashsale.exceptions.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SaleWindowClosedException.class)
	public ResponseEntity<ErrorResponseDto> handleSaleWindowClosedException(SaleWindowClosedException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.UNPROCESSABLE_ENTITY.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	@ExceptionHandler(OutOfStockException.class)
	public ResponseEntity<ErrorResponseDto> handleOutOfStockException(OutOfStockException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.CONFLICT.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(InvalidStateException.class)
	public ResponseEntity<ErrorResponseDto> handleInvalidStateException(InvalidStateException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(FlashSaleAppException.class)
	public ResponseEntity<ErrorResponseDto> handleFlashSaleAppException(FlashSaleAppException ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
		log.error(ex.getMessage(), ex);
		ErrorResponseDto erd = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value() , ex.getMessage());
		return new ResponseEntity<ErrorResponseDto>(erd, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
