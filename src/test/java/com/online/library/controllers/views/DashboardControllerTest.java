package com.online.library.controllers.views;

import com.online.library.domain.dto.BookDto;
import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.services.ReservationService;
import com.online.library.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private ReservationService reservationService;

        @Test
        public void testDashboardRequiresAuthentication() throws Exception {
                mockMvc.perform(get("/dashboard"))
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        public void testDashboardLoadsForAuthenticatedUser() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .email("test@example.com")
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findByUserIdOrderByDate(1L)).thenReturn(List.of());

                mockMvc.perform(get("/dashboard")
                                .with(user("testuser").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("dashboard"))
                                .andExpect(model().attributeExists("reservations"));
        }

        @Test
        public void testDashboardShowsReservations() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .email("test@example.com")
                                .build();

                BookDto book = BookDto.builder()
                                .id(1L)
                                .title("Test Book")
                                .build();

                ReservationDto reservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.OCZEKUJĄCA)
                                .user(user)
                                .book(book)
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findByUserIdOrderByDate(1L)).thenReturn(List.of(reservation));

                mockMvc.perform(get("/dashboard")
                                .with(user("testuser").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("dashboard"))
                                .andExpect(model().attribute("reservations", List.of(reservation)));
        }

        @Test
        public void testCancelReservation() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                ReservationDto reservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.OCZEKUJĄCA)
                                .user(user)
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findById(1L)).thenReturn(Optional.of(reservation));

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"));
        }

        @Test
        public void testCannotCancelOtherUsersReservation() throws Exception {
                UserResponseDto currentUser = UserResponseDto.builder()
                                .id(2L)
                                .username("anotheruser")
                                .build();

                UserResponseDto reservationOwner = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                ReservationDto reservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.OCZEKUJĄCA)
                                .user(reservationOwner)
                                .build();

                when(userService.findByUsername("anotheruser")).thenReturn(Optional.of(currentUser));
                when(reservationService.findById(1L)).thenReturn(Optional.of(reservation));

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("anotheruser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attribute("error", "Nie masz uprawnień do tej operacji."));
        }
}
