package com.online.library.controllers.views;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.dto.CommentDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.services.BookService;
import com.online.library.services.CommentService;
import com.online.library.services.RatingService;
import com.online.library.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookDetailsController {

    private final BookService bookService;
    private final RatingService ratingService;
    private final CommentService commentService;
    private final UserService userService;

    @GetMapping("/")
    public String mainPage(){
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String bookDetails(
            @PathVariable Long id,
            @RequestParam(name = "commentPage", defaultValue = "0") int commentPage,
            Principal principal,
            Model model) {

        BookDto book = bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        BigDecimal averageRating = ratingService.calculateAverageRating(id);
        Long ratingCount = ratingService.countRatingsForBook(id);

        Pageable commentPageable = PageRequest.of(commentPage, 10);
        Page<CommentDto> comments = commentService.findByBookId(id, commentPageable);
        Long commentCount = commentService.countCommentsForBook(id);

        model.addAttribute("book", book);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("ratingCount", ratingCount);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);

        if (principal != null) {
            UserResponseDto currentUser = userService.findByUsername(principal.getName())
                    .orElse(null);
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("canRate", ratingService.canUserRateBook(currentUser.getId(), id));
                Integer userRating = ratingService.getUserRatingForBook(currentUser.getId(), id);
                model.addAttribute("userRating", userRating);
            }
        }

        return "book-details";
    }

    @PostMapping("/{id}/rate")
    public String rateBook(
            @PathVariable Long id,
            @RequestParam("rating") Integer rating,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Musisz być zalogowany, aby ocenić książkę.");
            return "redirect:/login";
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("error", "Ocena musi być w zakresie od 1 do 5.");
            return "redirect:/books/" + id;
        }

        UserResponseDto currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            ratingService.addRating(currentUser.getId(), id, rating);
            redirectAttributes.addFlashAttribute("success", "Dziękujemy za ocenę!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Możesz ocenić tę książkę tylko raz na tydzień.");
        }

        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/comment")
    public String addComment(
            @PathVariable Long id,
            @RequestParam("content") String content,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "Musisz być zalogowany, aby dodać komentarz.");
            return "redirect:/login";
        }

        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Komentarz nie może być pusty.");
            return "redirect:/books/" + id;
        }

        UserResponseDto currentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        commentService.addComment(currentUser.getId(), id, content.trim());
        redirectAttributes.addFlashAttribute("success", "Komentarz został dodany!");

        return "redirect:/books/" + id;
    }
}
