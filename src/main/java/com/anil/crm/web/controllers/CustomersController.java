package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.services.CustomerService;
import com.anil.crm.web.models.CustomerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Tag(name = "Customers", description = "Müşteri kayıt (registration) ve profil işlemleri")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomersController {

    private final CustomerService customerService;

    @Operation(summary = "Bir müşteriyi ID ile getir")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Müşteri ID'si")
            @PathVariable Long id) {

        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Müşterileri listele veya filtrele",
            description = "Sistemdeki müşterileri listeler. Opsiyonel olarak 'email' (tam eşleşme) veya 'name' (ad/soyad içerir) " +
                    "query parametreleri ile filtreleme yapılabilir. 'email' parametresi önceliklidir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Müşteriler başarıyla listelendi/filtrelendi"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<CustomerDto>> findCustomers(
            @Parameter(description = "Filtrelemek için tam email adresi (opsiyonel)")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filtrelemek için ad veya soyad parçası (opsiyonel)")
            @RequestParam(required = false) String name
    ) {
        List<CustomerDto> result;

        if (email != null && !email.trim().isEmpty()) {
            try {
                CustomerDto customer = customerService.getCustomerByEmail(email);
                result = List.of(customer);
            } catch (ResourceNotFoundException e) {
                result = Collections.emptyList();
            }
        } else if (name != null && !name.trim().isEmpty()) {
            result = customerService.getCustomersByUserName(name);
        } else {
            result = customerService.getAllCustomers();
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Yeni bir müşteri kaydı oluştur (Registration)")
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(
            @Validated(CustomerDto.CreateValidation.class)
            @RequestBody CustomerDto customerDto) {


        CustomerDto savedCustomer = customerService.createCustomer(customerDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCustomer.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedCustomer);
    }

    @Operation(summary = "Bir müşterinin profil bilgilerini güncelle")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Güncellenecek müşteri ID'si")
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto) {


        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Bir müşteriyi sil")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Silinecek müşteri ID'si")
            @PathVariable Long id) {

        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }
}