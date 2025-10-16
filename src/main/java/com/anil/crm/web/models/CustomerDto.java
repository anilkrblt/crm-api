package com.anil.crm.web.models;

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
public class CustomerDto {

    @Null(message = "Id alanı girilemez, sistem tarafından atanacaktır")
    private Long id;

    @NotBlank(message = "İsim alanı boş olamaz")
    @Size(max = 100, message = "İsim 100 karakterden uzun olamaz")
    private String fullName;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String passwordHash;

    @NotBlank(message = "Telefon boş olamaz")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Geçerli bir telefon numarası giriniz")
    private String phone;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}