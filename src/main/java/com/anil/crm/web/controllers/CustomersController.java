package com.anil.crm.web.controllers;

import com.anil.crm.services.CustomerService;
import com.anil.crm.web.models.CustomerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // Validasyon import'u
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated; // Grup validasyonu için
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Customers", description = "Müşteri kayıt (registration) ve profil işlemleri")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class CustomersController {

    private final CustomerService customerService;

    @Operation(summary = "Bir müşteriyi ID ile getir")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Müşteri ID'si")
            @PathVariable Long id) {

        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Bir müşteriyi Email ile getir")
    @GetMapping("/email")
    public ResponseEntity<CustomerDto> getCustomerByEmail(
            @Parameter(description = "Müşterinin email adresi")
            @RequestParam String email) {

        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @Operation(summary = "Tüm müşterileri listele")
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @Operation(summary = "Müşterileri isme göre ara")
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDto>> searchCustomersByName(
            @Parameter(description = "Aranacak isim veya soyisim parçası")
            @RequestParam String name) {

        return ResponseEntity.ok(customerService.getCustomersByUserName(name));
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