package com.anil.crm.web.models;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDto {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @NotNull(message = "Müşteri bilgisi (ID) boş olamaz")
    private CustomerDto customer;

    @NotNull(message = "Departman bilgisi (ID) boş olamaz")
    private DepartmentDto department;

    private AgentDto assignedAgent;

    @NotBlank(message = "Konu boş olamaz")
    @Size(max = 255, message = "Konu 255 karakterden uzun olamaz")
    private String subject;

    @NotBlank(message = "Açıklama boş olamaz")
    private String description;

    @NotNull(message = "Durum boş olamaz")
    private TicketStatus status;

    @NotNull(message = "Öncelik boş olamaz")
    private TicketPriority priority;

}