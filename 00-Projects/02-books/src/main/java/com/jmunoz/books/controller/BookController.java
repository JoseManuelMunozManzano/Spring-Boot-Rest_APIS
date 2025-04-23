package com.jmunoz.books.controller;

import com.jmunoz.books.entity.Book;
import com.jmunoz.books.exception.BookNotFoundException;
import com.jmunoz.books.request.BookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Books Rest API Endpoints", description = "Operations related to books")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final List<Book> books = new ArrayList<>();

    public BookController() {
        initilizeBooks();
    }

    private void initilizeBooks() {
        books.addAll(List.of(
                new Book(1, "Computer Science Pro", "Chad Darby", "Computer Science", 5),
                new Book(2, "Java Spring Master", "Eric Roby", "Computer Science", 5),
                new Book(3, "Why 1+1 Rocks", "Adil A.", "Math", 5),
                new Book(4, "How Bears Hibernate", "Bob B.", "Science", 2),
                new Book(5, "A Pirate's Treasure", "Curt Sea", "History", 3),
                new Book(6, "Why 2+2 is Better", "Dan D.", "Math", 1)
        ));
    }

    // Por defecto, Spring Boot devuelve el estado HTTP Ok, pero está bien ser explícito por
    // si esto cambia.
    @Operation(summary = "Get all books", description = "Retrieve a list of all available books")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<Book> getBooks(@Parameter(description = "Optional query parameter")
                                   @RequestParam(required=false) String category) {
        if (category == null) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    // Validación de data usando el paquete Jakarta Validations Constraints.
    // En este caso @Min(value = 1)
    // Por defecto, Spring Boot devuelve el estado HTTP Ok, pero está bien ser explícito por
    // si esto cambia.
    @Operation(summary = "Get a book by ID", description = "Retrieve a specific book by ID")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public Book getBookById(@Parameter(description = "Id of book to be retrieved")
                                @PathVariable @Min(value = 1) long id) {

        // Validación de data usando Clausula Guarda.
        // El problema es que es muy tedioso si hay que validar muchos campos.
//        if (id < 1) {
//            return null;
//        }

        // Con .orElseThrow() lanzamos una excepción si no encontramos el book.
        // Por si solo no devuelve BookErrorResponse. Tenemos que crear un méto-do anotado
        // con @ExceptionHandler (ver méto-do) que devuelve ResponseEntity<BookErrorResponse>
        // Esta opción se ha comentado.
        //
        // Otra opción que es la que queda activa es crear una clase anotada con @ControllerAdvice,
        // que gestiona excepciones y sirve para todos los controllers.
        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found - " + id));
    }

    // Notar que un POST necesita crear un id, no pasarle el id en el body.
    // Si permitimos que el usuario pasa el id podríamos romper la clave unique.
    // Para ello se ha creado el DTO BookRequest indicando la anotación @RequestBody
    // Para tener validaciones de data se añade la anotación @Valid antes de la
    // anotación @RequestBody
    // Y las validaciones se indican en el Request Objects.
    // Usamos la anotación @RequestStatus para indicar el estado de la respuesta que deseamos si to-do va bien.
    @Operation(summary = "Create a new book", description = "Add a new book to the list")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createBook(@Valid @RequestBody BookRequest bookRequest) {
        long id = books.isEmpty() ? 1 : books.getLast().getId() + 1;
        Book book = convertToBook(id, bookRequest);
        books.add(book);
    }

    // No content indica que devolvemos void.
    @Operation(summary = "Update a book", description = "Update the details of an existing book")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public Book updateBook(@Parameter(description = "Id of the book to update")
                               @PathVariable @Min(value = 1) long id,
                           @Valid @RequestBody BookRequest bookRequest) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId() == id) {
                Book updatedBook = convertToBook(id, bookRequest);
                books.set(i, updatedBook);
                return updatedBook;
            }
        }

        // Si book no fue encontrado en el bucle de arriba lanzamos la excepción.
        throw new BookNotFoundException("Book not found - " + id);
    }

    // No content indica que devolvemos void.
    @Operation(summary = "Delete a book", description = "Remove a book from the list")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteBook(@Parameter(description = "Id of the book to delete")
            @PathVariable @Min(value = 1) long id) {

        // Si book no existe lanza la excepción.
        books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found - " + id));

        books.removeIf(book -> book.getId() == id);
    }

    private Book convertToBook(long id, BookRequest bookRequest) {
        return new Book(
                id,
                bookRequest.getTitle(),
                bookRequest.getAuthor(),
                bookRequest.getCategory(),
                bookRequest.getRating()
        );
    }

    // Cuando ocurre la excepción BookNotFoundException, llamamos a este handler Exception.
    // Este méto-do es el que devuelve BookErrorResponse.
    // Solo funciona en este controller.
    //
    // Comentado porque lo cambiamos para usar la clase BookExceptionHandler,
    // que es posible usar en cualquier controller.
    // Nos llevamos el código que vemos aquí
//    @ExceptionHandler
//    public ResponseEntity<BookErrorResponse> handleException(BookNotFoundException exc) {
//        BookErrorResponse bookErrorResponse = new BookErrorResponse(
//                HttpStatus.NOT_FOUND.value(),
//                exc.getMessage(),
//                System.currentTimeMillis()
//        );
//
//        return new ResponseEntity<>(bookErrorResponse, HttpStatus.NOT_FOUND);
//    }

    // Excepción Global que devuelve BAD_REQUEST
    // Para todos los tipos de excepción salvo BookNotFoundException.
    // Notar que en mensaje no se da mucha información para evitar fugas de información.
    // Solo funciona en este controller.
    //
    // Comentado porque lo cambiamos para usar la clase BookExceptionHandler,
    // que es posible usar en cualquier controller.
    // Nos llevamos el código que vemos aquí
//    @ExceptionHandler
//    public ResponseEntity<BookErrorResponse> handleException(Exception exc) {
//        BookErrorResponse bookErrorResponse = new BookErrorResponse(
//                HttpStatus.BAD_REQUEST.value(),
//                "Invalid request",
//                System.currentTimeMillis()
//        );
//
//        return new ResponseEntity<>(bookErrorResponse, HttpStatus.BAD_REQUEST);
//    }
}
