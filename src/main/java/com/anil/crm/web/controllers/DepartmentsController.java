package com.anil.crm.web.controllers;

import com.anil.crm.services.DepartmentService;
import com.anil.crm.web.models.DepartmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Departments", description = "Departman yönetimi endpointleri")
@RestController
@RequestMapping("/api/departments")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DepartmentsController {

    private final DepartmentService departmentService;

    @Operation(summary = "Yeni bir departman oluştur (Sadece Admin)", description = "Yeni bir departman kaydı ekler.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Departman başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz departman verisi (örn: isim boş)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Departman adı zaten mevcut", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Parameter(description = "Oluşturulacak departman bilgileri (isim zorunlu)") @Valid @RequestBody DepartmentDto departmentDto) {

        DepartmentDto savedDepartment = departmentService.createDepartment(departmentDto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedDepartment.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedDepartment);
    }


    @Operation(summary = "Bir departmanı ID ile getir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Departman bulundu"),
            @ApiResponse(responseCode = "404", description = "Departman bulunamadı", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@Parameter(description = "Aranan departmanın ID'si") @PathVariable Long id) {

        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }


    @Operation(summary = "Departmanları listele veya isme göre filtrele", description = "Sistemdeki tüm departmanları listeler. 'name' parametresi verilirse, adı o metni içeren departmanları filtreler.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Departmanlar listelendi/filtrelendi"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllOrSearchDepartments(@Parameter(description = "Filtrelemek için departman adı parçası (opsiyonel)") @RequestParam(required = false) String name) {

        List<DepartmentDto> departments;
        if (name != null && !name.trim().isEmpty()) {
            log.debug("Searching departments with name containing: {}", name);
            departments = departmentService.searchDepartmentsByName(name);
        } else {
            departments = departmentService.getAllDepartments();
        }
        return ResponseEntity.ok(departments);
    }


    @Operation(summary = "Mevcut bir departmanı güncelle (Sadece Admin)", description = "Bir departmanın adını veya açıklamasını günceller.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Departman başarıyla güncellendi"),
            @ApiResponse(responseCode = "400", description = "Geçersiz departman verisi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Departman bulunamadı", content = @Content),
            @ApiResponse(responseCode = "409", description = "Yeni departman adı zaten mevcut", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @Parameter(description = "Güncellenecek departmanın ID'si")
            @PathVariable Long id,
            @Parameter(description = "Güncel departman bilgileri")
            @Valid @RequestBody DepartmentDto departmentDto) {

        return ResponseEntity.ok(departmentService.updateDepartment(id, departmentDto));
    }


    @Operation(summary = "Bir departmanı sil (Sadece Admin)", description = "Bir departmanı ID'si ile siler. Departmana atanmış Ajan varsa silme işlemi başarısız olur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Departman başarıyla silindi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Departman bulunamadı", content = @Content),
            @ApiResponse(responseCode = "409", description = "Departman Ajanlar tarafından kullanılıyor", content = @Content),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Silinecek departmanın ID'si")
            @PathVariable Long id) {

        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}