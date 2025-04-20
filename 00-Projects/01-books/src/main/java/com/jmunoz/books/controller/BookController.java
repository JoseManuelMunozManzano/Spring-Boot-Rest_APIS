package com.jmunoz.books.controller;

import com.jmunoz.books.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final List<Book> books = new ArrayList<>();

    public BookController() {
        initilizeBooks();
    }

    private void initilizeBooks() {
        books.addAll(List.of(
                new Book("Title one", "Author one", "science"),
                new Book("Title two", "Author two", "science"),
                new Book("Title three", "Author three", "history"),
                new Book("Title four", "Author four", "math"),
                new Book("Title five", "Author five", "math"),
                new Book("Title six", "Author six", "math")
        ));
    }

    @GetMapping("/hello")
    public String firstAPI() {
        return "Hola José Manuel!";
    }

    // Comentamos este méto-do porque su URL choca con getBooksByCategory() más abajo.
//    @GetMapping("/api/books")
//    public List<Book> getBooks() {
//        return books;
//    }

    // Path Variables (o Path Parameter)
    // En el parámetro de la función tenemos que indicar @PathVariable
    // El nombre que se indique entre llaves tiene que ser el mismo que el nombre
    // del parámetro de la función.
    @GetMapping("/{title}")
    public Book getBookByTitle(@PathVariable String title) {

        // Programación imperativa
//        for (Book book : books) {
//            if (book.getTitle().equalsIgnoreCase(title)) {
//                return book;
//            }
//        }

//        return null;

        // Programación funcional
        return books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    // Request Parameters
    // En la URL no se indica el Request Parameter.
    // En el parámetro de la función tenemos que indicar @RequestParam y es donde viene
    // el Request Parameter.
    // En este caso, no es obligatorio informarlo.
    //
    // Cuando no lo informamos, la URL choca con el méto-do public List<Book> getBooks()
    // ya que ambos son GET.
    // Por eso se ha comentado.
    @GetMapping
    public List<Book> getBooksByCategory(@RequestParam(required=false) String category) {

        if (category == null) {
            return books;
        }

        // Programación imperativa
//        List<Book> filteredBooks = new ArrayList<>();
//
//        for (Book book : books) {
//            if (book.getCategory().equalsIgnoreCase(category)) {
//                filteredBooks.add(book);
//            }
//        }
//
//        return filteredBooks;

        // Programación funcional
        return books.stream()
                .filter(book -> book.getCategory().equalsIgnoreCase(category))
                .toList();
    }

    @PostMapping
    public void createBook(@RequestBody Book newBook) {

        // Programación imperativa
//        for (Book book : books) {
//            if (book.getTitle().equalsIgnoreCase(newBook.getTitle())) {
//                return;
//            }
//        }
//        books.add(newBook);

        // Programación funcional
        boolean isNewBook = books.stream()
                        .noneMatch(book -> book.getTitle().equalsIgnoreCase(newBook.getTitle()));
        if (isNewBook) {
            books.add(newBook);
        }
    }

    @PutMapping("/{title}")
    public void updateBook(@PathVariable String title, @RequestBody Book updatedBook) {

        // Solo con programación imperativa porque en este caso es más eficiente.
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getTitle().equalsIgnoreCase(title)) {
                books.set(i, updatedBook);
                return;
            }
        }
    }

    @DeleteMapping("/{title}")
    public void deleteBook(@PathVariable String title) {
        books.removeIf(book -> book.getTitle().equalsIgnoreCase(title));
    }
}
