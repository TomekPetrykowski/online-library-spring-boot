package com.online.library.controllers.views;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.services.ReservationService;
import com.online.library.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        List<ReservationDto> reservations = reservationService.findByUserIdOrderByDate(user.getId());

        model.addAttribute("reservations", reservations);

        return "dashboard";
    }

    @PostMapping("/dashboard/reservations/{id}/cancel")
    public String cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            verifyOwnership(id, userDetails);
            reservationService.cancelReservation(id);
            redirectAttributes.addFlashAttribute("success", "Rezerwacja została anulowana.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/reservations/{id}/confirm")
    public String confirmReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            verifyOwnership(id, userDetails);
            reservationService.changeStatus(id, ReservationStatus.POTWIERDZONA);
            redirectAttributes.addFlashAttribute("success", "Rezerwacja została potwierdzona.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard";
    }

    private void verifyOwnership(Long reservationId, UserDetails userDetails) {
        UserResponseDto user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ReservationDto reservation = reservationService.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Nie masz uprawnień do tej operacji.");
        }
    }
}
