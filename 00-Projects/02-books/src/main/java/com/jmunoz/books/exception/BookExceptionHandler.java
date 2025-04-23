package com.jmunoz.books.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// Con esta anotación permitimos que esta excepción pueda usarse
// en todos los controllers.
@ControllerAdvice
public class BookExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<BookErrorResponse> handleException(BookNotFoundException exc) {
        BookErrorResponse bookErrorResponse = new BookErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exc.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(bookErrorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<BookErrorResponse> handleException(Exception exc) {
        BookErrorResponse bookErrorResponse = new BookErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(bookErrorResponse, HttpStatus.BAD_REQUEST);
    }
}
