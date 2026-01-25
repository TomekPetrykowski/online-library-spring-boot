package com.online.library.controllers.api;

import com.online.library.domain.dto.UserRequestDto;
import com.online.library.domain.dto.UserResponseDto;
import com.online.library.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API do zarządzania użytkownikami")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Utwórz użytkownika", description = "Tworzy nowego użytkownika w systemie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Użytkownik utworzony", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userDto) {
        UserResponseDto savedUserDto = userService.save(userDto);
        return new ResponseEntity<>(savedUserDto, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lista użytkowników", description = "Pobiera paginowaną listę wszystkich użytkowników")
    @ApiResponse(responseCode = "200", description = "Lista użytkowników")
    public Page<UserResponseDto> listUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Pobierz użytkownika", description = "Pobiera szczegóły użytkownika po ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik znaleziony", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony")
    })
    public ResponseEntity<UserResponseDto> getUser(
            @Parameter(description = "ID użytkownika") @PathVariable("id") Long id) {
        return userService.findById(id)
                .map(userDto -> new ResponseEntity<>(userDto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aktualizuj użytkownika", description = "Pełna aktualizacja użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
    })
    public ResponseEntity<UserResponseDto> fullUpdateUser(
            @Parameter(description = "ID użytkownika") @PathVariable("id") Long id,
            @Valid @RequestBody UserRequestDto userDto) {
        if (!userService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        userDto.setId(id);
        UserResponseDto savedUserDto = userService.save(userDto);
        return new ResponseEntity<>(savedUserDto, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Częściowa aktualizacja użytkownika", description = "Aktualizuje wybrane pola użytkownika")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Użytkownik zaktualizowany"),
            @ApiResponse(responseCode = "404", description = "Użytkownik nie znaleziony")
    })
    public ResponseEntity<UserResponseDto> partialUpdateUser(
            @Parameter(description = "ID użytkownika") @PathVariable("id") Long id,
            @RequestBody UserRequestDto userDto) {
        if (!userService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        UserResponseDto updatedUser = userService.partialUpdate(id, userDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Usuń użytkownika", description = "Usuwa użytkownika z systemu")
    @ApiResponse(responseCode = "204", description = "Użytkownik usunięty")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID użytkownika") @PathVariable("id") Long id) {
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
