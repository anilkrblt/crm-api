package com.anil.crm.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    private Long id;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotBlank(message = "İsim alanı boş olamaz")
    @Size(max = 100, message = "İsim 100 karakterden uzun olamaz")
    private String firstName;

    @NotBlank(message = "Soyisim alanı boş olamaz")
    @Size(max = 100, message = "Soyisim 100 karakterden uzun olamaz")
    private String lastName;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email giriniz")
    private String email;

    @NotBlank(message = "Telefon boş olamaz")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Geçerli bir telefon numarası giriniz (örn: +905551234567)")
    private String phone;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Şifre boş olamaz", groups = CreateValidation.class)
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;


    public interface CreateValidation {}
}