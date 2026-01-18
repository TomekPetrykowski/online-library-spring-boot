package com.online.library.controllers.views;

import com.online.library.domain.dto.BookDto;
import com.online.library.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final BookService bookService;

    @GetMapping("/")
    public String home(
            @RequestParam(name = "q", required = false) String searchTerm,
            @RequestParam(name = "sort", defaultValue = "title") String sortField,
            @RequestParam(name = "dir", defaultValue = "asc") String sortDir,
            Pageable pageable,
            Model model) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<BookDto> books;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            books = bookService.searchBooks(searchTerm, sortedPageable);
        } else {
            books = bookService.findAll(sortedPageable);
        }

        Page<BookDto> popularBooks = bookService.getPopularBooks(PageRequest.of(0, 5));

        model.addAttribute("books", books);
        model.addAttribute("popularBooks", popularBooks);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "index";
    }
}
