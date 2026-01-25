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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private ReservationService reservationService;

        @Test
        void testDashboardRequiresAuthentication() throws Exception {
                mockMvc.perform(get("/dashboard"))
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        void testDashboardLoadsForAuthenticatedUser() throws Exception {
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

                verify(userService).findByUsername("testuser");
                verify(reservationService).findByUserIdOrderByDate(1L);
        }

        @Test
        void testDashboardShowsReservations() throws Exception {
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

                verify(userService).findByUsername("testuser");
                verify(reservationService).findByUserIdOrderByDate(1L);
        }

        @Test
        void testDashboardUserNotFound() throws Exception {
                when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

                // The controller throws RuntimeException when user is not found
                // Since there's no exception handler for RuntimeException in view controllers,
                // the exception propagates and causes a nested exception
                Exception exception = assertThrows(Exception.class, () -> {
                        mockMvc.perform(get("/dashboard")
                                        .with(user("testuser").roles("USER")));
                });

                assertTrue(exception.getCause() instanceof RuntimeException);
                assertEquals("User not found", exception.getCause().getMessage());

                verify(userService).findByUsername("testuser");
                verify(reservationService, never()).findByUserIdOrderByDate(anyLong());
        }

        @Test
        void testCancelReservationSuccess() throws Exception {
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
                doNothing().when(reservationService).cancelReservation(1L);

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).findByUsername("testuser");
                verify(reservationService).findById(1L);
                verify(reservationService).cancelReservation(1L);
        }

        @Test
        void testCannotCancelOtherUsersReservation() throws Exception {
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

                verify(userService).findByUsername("anotheruser");
                verify(reservationService).findById(1L);
                verify(reservationService, never()).cancelReservation(anyLong());
        }

        @Test
        void testConfirmReservationSuccess() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                ReservationDto reservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.OCZEKUJĄCA)
                                .user(user)
                                .build();

                ReservationDto confirmedReservation = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.POTWIERDZONA)
                                .user(user)
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findById(1L)).thenReturn(Optional.of(reservation));
                when(reservationService.changeStatus(1L, ReservationStatus.POTWIERDZONA))
                                .thenReturn(confirmedReservation);

                mockMvc.perform(post("/dashboard/reservations/1/confirm")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("success"));

                verify(userService).findByUsername("testuser");
                verify(reservationService).findById(1L);
                verify(reservationService).changeStatus(1L, ReservationStatus.POTWIERDZONA);
        }

        @Test
        void testCannotConfirmOtherUsersReservation() throws Exception {
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

                mockMvc.perform(post("/dashboard/reservations/1/confirm")
                                .with(user("anotheruser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attribute("error", "Nie masz uprawnień do tej operacji."));

                verify(userService).findByUsername("anotheruser");
                verify(reservationService).findById(1L);
                verify(reservationService, never()).changeStatus(anyLong(), any());
        }

        @Test
        void testCancelReservationUserNotFound() throws Exception {
                when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).findByUsername("testuser");
                verify(reservationService, never()).cancelReservation(anyLong());
        }

        @Test
        void testCancelReservationNotFound() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findById(1L)).thenReturn(Optional.empty());

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).findByUsername("testuser");
                verify(reservationService).findById(1L);
                verify(reservationService, never()).cancelReservation(anyLong());
        }

        @Test
        void testConfirmReservationUserNotFound() throws Exception {
                when(userService.findByUsername("testuser")).thenReturn(Optional.empty());

                mockMvc.perform(post("/dashboard/reservations/1/confirm")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).findByUsername("testuser");
                verify(reservationService, never()).changeStatus(anyLong(), any());
        }

        @Test
        void testConfirmReservationNotFound() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findById(1L)).thenReturn(Optional.empty());

                mockMvc.perform(post("/dashboard/reservations/1/confirm")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(userService).findByUsername("testuser");
                verify(reservationService).findById(1L);
                verify(reservationService, never()).changeStatus(anyLong(), any());
        }

        @Test
        void testCancelReservationServiceError() throws Exception {
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
                doThrow(new RuntimeException("Cannot cancel reservation"))
                                .when(reservationService).cancelReservation(1L);

                mockMvc.perform(post("/dashboard/reservations/1/cancel")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(reservationService).cancelReservation(1L);
        }

        @Test
        void testConfirmReservationServiceError() throws Exception {
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
                doThrow(new RuntimeException("Cannot confirm reservation"))
                                .when(reservationService).changeStatus(1L, ReservationStatus.POTWIERDZONA);

                mockMvc.perform(post("/dashboard/reservations/1/confirm")
                                .with(user("testuser").roles("USER"))
                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/dashboard"))
                                .andExpect(flash().attributeExists("error"));

                verify(reservationService).changeStatus(1L, ReservationStatus.POTWIERDZONA);
        }

        @Test
        void testDashboardWithMultipleReservations() throws Exception {
                UserResponseDto user = UserResponseDto.builder()
                                .id(1L)
                                .username("testuser")
                                .build();

                BookDto book1 = BookDto.builder().id(1L).title("Book 1").build();
                BookDto book2 = BookDto.builder().id(2L).title("Book 2").build();

                ReservationDto reservation1 = ReservationDto.builder()
                                .id(1L)
                                .status(ReservationStatus.OCZEKUJĄCA)
                                .user(user)
                                .book(book1)
                                .build();

                ReservationDto reservation2 = ReservationDto.builder()
                                .id(2L)
                                .status(ReservationStatus.POTWIERDZONA)
                                .user(user)
                                .book(book2)
                                .build();

                when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));
                when(reservationService.findByUserIdOrderByDate(1L))
                                .thenReturn(List.of(reservation1, reservation2));

                mockMvc.perform(get("/dashboard")
                                .with(user("testuser").roles("USER")))
                                .andExpect(status().isOk())
                                .andExpect(view().name("dashboard"))
                                .andExpect(model().attribute("reservations", List.of(reservation1, reservation2)));

                verify(reservationService).findByUserIdOrderByDate(1L);
        }
}
