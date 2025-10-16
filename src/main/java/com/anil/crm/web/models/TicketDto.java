package com.anil.crm.web.models;

import com.anil.crm.domain.Agent;
import com.anil.crm.domain.Customer;
import jakarta.validation.constraints.*;
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

    @Null(message = "Id alanı girilemez, sistem tarafından atanacaktır")
    private Long id;

    @NotNull(message = "Müşteri bilgisi boş olamaz")
    private Customer customer;

    private Agent agent;

    @NotBlank(message = "Konu boş olamaz")
    @Size(max = 255, message = "Konu 255 karakterden uzun olamaz")
    private String subject;

    @NotBlank(message = "Açıklama boş olamaz")
    private String description;

    @NotBlank(message = "Durum boş olamaz")
    @Pattern(regexp = "OPEN|PENDING|RESOLVED", message = "Durum OPEN, PENDING veya RESOLVED olmalıdır")
    private String status;

    @NotBlank(message = "Öncelik boş olamaz")
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Öncelik LOW, MEDIUM veya HIGH olmalıdır")
    private String priority;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
