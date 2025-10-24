package com.anil.crm.web.models;

import com.anil.crm.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketCommentDto {

    private Long id;
    private LocalDateTime createdAt;

    private String authorFirstName;

    private String authorLastName;

    private Role authorRole;


    @NotNull(message = "Ticket ID boş olamaz")
    private Long ticketId;

    @NotBlank(message = "Yorum boş olamaz")
    private String comment;
}