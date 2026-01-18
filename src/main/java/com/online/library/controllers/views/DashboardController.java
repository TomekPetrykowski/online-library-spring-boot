package com.online.library.controllers.views;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.services.ReservationService;
import com.online.library.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        UserResponseDto user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<ReservationDto> reservations = reservationService.findByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("reservations", reservations);
        
        return "dashboard";
    }
}
