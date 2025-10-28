package com.anil.crm.web.models;

import com.anil.crm.domain.TicketPriority;
import com.anil.crm.domain.TicketStatus;
// import jakarta.validation.Valid; // Artık iç içe validasyon için kullanılmıyor
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Bilet (Ticket) verilerini taşımak için DTO.
 * Departman ve atanmış ajanı içerir.
 * İç içe nesnelerin validasyonu POST işleminde atlanır.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDto {

    // --- Yanıt Alanları ---
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- İlişkili Nesneler (DTO olarak) ---

    /** Müşteri bilgisi (ID'si POST isteğinde zorunlu) */
    @NotNull(message = "Müşteri bilgisi (ID) boş olamaz")
    // @Valid // <-- KALDIRILDI / YORUM SATIRI YAPILDI
    private CustomerDto customer;

    /** Biletin ait olduğu departman (ID'si POST isteğinde zorunlu) */
    @NotNull(message = "Departman bilgisi (ID) boş olamaz")
    // @Valid // <-- KALDIRILDI / YORUM SATIRI YAPILDI
    private DepartmentDto department;

    /** Bileti üstlenen ajan (Boş olabilir) */
    // @Valid // <-- KALDIRILDI / YORUM SATIRI YAPILDI
    private AgentDto assignedAgent;

    // --- Bilet Detayları ---

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