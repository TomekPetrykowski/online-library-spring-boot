package com.online.library.controllers.views;

import com.online.library.domain.dto.UserLoginRequestDto;
import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.enums.UserRole;
import com.online.library.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Nieprawidłowa nazwa użytkownika lub hasło");
        }
        if (logout != null) {
            model.addAttribute("message", "Wylogowano pomyślnie");
        }

        model.addAttribute("loginRequest", new UserLoginRequestDto());
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRequestDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") UserRequestDto userDto,
            BindingResult result,
            Model model) {

        if (userService.findByUsername(userDto.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Użytkownik o takiej nazwie już istnieje");
        }

        if (result.hasErrors()) {
            log.info(result.getAllErrors().toString());
            return "register";
        }

        userDto.setRole(UserRole.USER);
        userDto.setEnabled(true);
        userService.save(userDto);

        return "redirect:/login?registered";
    }
}
