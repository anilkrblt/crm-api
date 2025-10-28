package com.anil.crm.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class AgentDto {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotBlank(message = "İsim alanı boş olamaz")
    private String firstName;

    @NotBlank(message = "Soyisim alanı boş olamaz")
    private String lastName;

    @Email
    @NotBlank(message = "Email boş olamaz")
    private String email;

    @NotBlank(message = "Departman adı boş olamaz", groups = CreateValidation.class)
    private String departmentName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Şifre boş olamaz", groups = CreateValidation.class)
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    public interface CreateValidation {}
}