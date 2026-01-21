package com.online.library.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorStatDto {
    private Long id;
    private String name;
    private String lastName;
    private Long loanCount;
}
