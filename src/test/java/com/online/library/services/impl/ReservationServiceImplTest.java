package com.online.library.services.impl;

import com.online.library.domain.dto.ReservationDto;
import com.online.library.domain.entities.ReservationEntity;
import com.online.library.domain.enums.ReservationStatus;
import com.online.library.exceptions.ResourceNotFoundException;
import com.online.library.mappers.Mapper;
import com.online.library.repositories.ReservationRepository;
import com.online.library.utils.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private Mapper<ReservationEntity, ReservationDto> reservationMapper;

    @InjectMocks
    private ReservationServiceImpl underTest;

    @Test
    public void testThatReservationIsSavedSuccessfully() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        ReservationDto reservationDto = ReservationDto.builder().status(reservationEntity.getStatus()).build();

        when(reservationMapper.mapFrom(reservationDto)).thenReturn(reservationEntity);
        when(reservationRepository.save(reservationEntity)).thenReturn(reservationEntity);
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        ReservationDto result = underTest.save(reservationDto);

        assertThat(result).isEqualTo(reservationDto);
        verify(reservationRepository, times(1)).save(reservationEntity);
    }

    @Test
    public void testThatFindAllReturnsListOfReservations() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        ReservationDto reservationDto = ReservationDto.builder().id(1L).status(ReservationStatus.OCZEKUJĄCA).build();

        when(reservationRepository.findAll()).thenReturn(List.of(reservationEntity));
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        List<ReservationDto> result = underTest.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(reservationDto);
    }

    @Test
    public void testThatFindAllWithPageableReturnsPageOfReservations() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        ReservationDto reservationDto = ReservationDto.builder().id(1L).status(ReservationStatus.OCZEKUJĄCA).build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<ReservationEntity> reservationPage = new PageImpl<>(List.of(reservationEntity));

        when(reservationRepository.findAll(pageable)).thenReturn(reservationPage);
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        Page<ReservationDto> result = underTest.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(reservationDto);
    }

    @Test
    public void testThatFindByIdReturnsReservationWhenExists() {
        ReservationEntity reservationEntity = TestDataUtil.createTestReservation(null, null);
        reservationEntity.setId(1L);
        ReservationDto reservationDto = ReservationDto.builder().id(1L).status(ReservationStatus.OCZEKUJĄCA).build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationEntity));
        when(reservationMapper.mapTo(reservationEntity)).thenReturn(reservationDto);

        Optional<ReservationDto> result = underTest.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(reservationDto);
    }

    @Test
    public void testThatFindByIdReturnsEmptyWhenNotExists() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<ReservationDto> result = underTest.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    public void testThatIsExistsReturnsTrueWhenExists() {
        when(reservationRepository.existsById(1L)).thenReturn(true);

        boolean result = underTest.isExists(1L);

        assertThat(result).isTrue();
    }

    @Test
    public void testThatPartialUpdateUpdatesReservationSuccessfully() {
        Long reservationId = 1L;
        ReservationEntity existingReservation = TestDataUtil.createTestReservation(null, null);
        existingReservation.setId(reservationId);

        ReservationDto updateDto = ReservationDto.builder().status(ReservationStatus.POTWIERDZONA).build();
        ReservationDto updatedDto = ReservationDto.builder().id(reservationId).status(ReservationStatus.POTWIERDZONA).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(existingReservation);
        when(reservationMapper.mapTo(any(ReservationEntity.class))).thenReturn(updatedDto);

        ReservationDto result = underTest.partialUpdate(reservationId, updateDto);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.POTWIERDZONA);
        verify(reservationRepository, times(1)).save(existingReservation);
    }

    @Test
    public void testThatPartialUpdateThrowsExceptionWhenReservationDoesNotExist() {
        Long reservationId = 1L;
        ReservationDto updateDto = ReservationDto.builder().status(ReservationStatus.POTWIERDZONA).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.partialUpdate(reservationId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Reservation does not exist");
    }

    @Test
    public void testThatDeleteCallsRepository() {
        Long reservationId = 1L;
        doNothing().when(reservationRepository).deleteById(reservationId);

        underTest.delete(reservationId);

        verify(reservationRepository, times(1)).deleteById(reservationId);
    }
}
