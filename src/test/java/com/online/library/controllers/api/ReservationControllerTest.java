package com.online.library.controllers.api;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController underTest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(underTest)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCreateReservationReturns201Created() throws Exception {
        // Given
        ReservationDto reservationDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .reservedAt(LocalDateTime.now())
                .build();

        when(reservationService.save(any(ReservationDto.class))).thenReturn(reservationDto);

        String reservationJson = """
                {"user":{"id":1},"book":{"id":1},"status":"OCZEKUJĄCA"}
                """;

        // When/Then
        mockMvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OCZEKUJĄCA"));

        verify(reservationService).save(any(ReservationDto.class));
    }

    @Test
    void testListReservationsReturnsPage() throws Exception {
        // Given
        ReservationDto reservation1 = ReservationDto.builder()
                .id(1L).status(ReservationStatus.OCZEKUJĄCA).build();
        ReservationDto reservation2 = ReservationDto.builder()
                .id(2L).status(ReservationStatus.POTWIERDZONA).build();
        ReservationDto reservation3 = ReservationDto.builder()
                .id(3L).status(ReservationStatus.WYPOŻYCZONA).build();
        Page<ReservationDto> reservationPage = new PageImpl<>(
                List.of(reservation1, reservation2, reservation3), PageRequest.of(0, 10), 3);

        when(reservationService.findAll(any())).thenReturn(reservationPage);

        // When/Then
        mockMvc.perform(get("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].status").value("OCZEKUJĄCA"))
                .andExpect(jsonPath("$.content[1].status").value("POTWIERDZONA"))
                .andExpect(jsonPath("$.content[2].status").value("WYPOŻYCZONA"));

        verify(reservationService).findAll(any());
    }

    @Test
    void testGetReservationByIdReturns200WhenFound() throws Exception {
        // Given
        ReservationDto reservationDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.OCZEKUJĄCA)
                .reservedAt(LocalDateTime.now())
                .build();

        when(reservationService.findById(1L)).thenReturn(Optional.of(reservationDto));

        // When/Then
        mockMvc.perform(get("/api/v1/reservations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OCZEKUJĄCA"));

        verify(reservationService).findById(1L);
    }

    @Test
    void testGetReservationByIdReturns404WhenNotFound() throws Exception {
        // Given
        when(reservationService.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/v1/reservations/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(reservationService).findById(999L);
    }

    @Test
    void testFullUpdateReservationReturns200WhenExists() throws Exception {
        // Given
        ReservationDto updatedDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.POTWIERDZONA)
                .build();

        when(reservationService.isExists(1L)).thenReturn(true);
        when(reservationService.save(any(ReservationDto.class))).thenReturn(updatedDto);

        String reservationJson = """
                {"user":{"id":1},"book":{"id":1},"status":"POTWIERDZONA"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POTWIERDZONA"));

        verify(reservationService).isExists(1L);
        verify(reservationService).save(any(ReservationDto.class));
    }

    @Test
    void testFullUpdateReservationReturns404WhenNotExists() throws Exception {
        // Given
        when(reservationService.isExists(999L)).thenReturn(false);

        String reservationJson = """
                {"user":{"id":1},"book":{"id":1},"status":"OCZEKUJĄCA"}
                """;

        // When/Then
        mockMvc.perform(put("/api/v1/reservations/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reservationJson))
                .andExpect(status().isNotFound());

        verify(reservationService).isExists(999L);
        verify(reservationService, never()).save(any());
    }

    @Test
    void testPartialUpdateReservationReturns200WhenExists() throws Exception {
        // Given
        ReservationDto updatedDto = ReservationDto.builder()
                .id(1L)
                .status(ReservationStatus.POTWIERDZONA)
                .confirmedAt(LocalDateTime.now())
                .build();

        when(reservationService.isExists(1L)).thenReturn(true);
        when(reservationService.partialUpdate(eq(1L), any(ReservationDto.class))).thenReturn(updatedDto);

        String patchJson = """
                {"status":"POTWIERDZONA"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/reservations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POTWIERDZONA"));

        verify(reservationService).isExists(1L);
        verify(reservationService).partialUpdate(eq(1L), any(ReservationDto.class));
    }

    @Test
    void testPartialUpdateReservationReturns404WhenNotExists() throws Exception {
        // Given
        when(reservationService.isExists(999L)).thenReturn(false);

        String patchJson = """
                {"status":"POTWIERDZONA"}
                """;

        // When/Then
        mockMvc.perform(patch("/api/v1/reservations/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isNotFound());

        verify(reservationService).isExists(999L);
        verify(reservationService, never()).partialUpdate(any(), any());
    }

    @Test
    void testDeleteReservationReturns204() throws Exception {
        // Given
        doNothing().when(reservationService).delete(1L);

        // When/Then
        mockMvc.perform(delete("/api/v1/reservations/1"))
                .andExpect(status().isNoContent());

        verify(reservationService).delete(1L);
    }

    @Test
    void testListReservationsWithPagination() throws Exception {
        // Given
        ReservationDto reservation = ReservationDto.builder()
                .id(1L).status(ReservationStatus.OCZEKUJĄCA).build();
        Page<ReservationDto> reservationPage = new PageImpl<>(
                List.of(reservation), PageRequest.of(0, 5), 10);

        when(reservationService.findAll(any())).thenReturn(reservationPage);

        // When/Then
        mockMvc.perform(get("/api/v1/reservations")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.size").value(5));

        verify(reservationService).findAll(any());
    }

    @Test
    void testCreateReservationWithAllStatuses() throws Exception {
        // Test each valid status
        for (ReservationStatus status : ReservationStatus.values()) {
            ReservationDto reservationDto = ReservationDto.builder()
                    .id(1L)
                    .status(status)
                    .build();

            when(reservationService.save(any(ReservationDto.class))).thenReturn(reservationDto);

            String reservationJson = String.format("""
                    {"user":{"id":1},"book":{"id":1},"status":"%s"}
                    """, status.name());

            mockMvc.perform(post("/api/v1/reservations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reservationJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(status.name()));
        }

        verify(reservationService, times(4)).save(any(ReservationDto.class));
    }
}
