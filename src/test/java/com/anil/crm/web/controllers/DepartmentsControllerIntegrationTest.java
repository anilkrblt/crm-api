package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.DepartmentNameExistsException;
import com.anil.crm.exceptions.ResourceInUseException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.services.DepartmentService;
import com.anil.crm.services.JwtService;
import com.anil.crm.services.UserDetailsServiceImpl;
import com.anil.crm.web.models.DepartmentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepartmentsController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
class DepartmentsControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DepartmentService departmentService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    DepartmentDto testDepartmentDto;

    @BeforeEach
    void setUp() {
        testDepartmentDto = DepartmentDto.builder()
                .id(1L)
                .name("Teknik Destek")
                .description("Test departmanı")
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createDepartment() throws Exception {
        DepartmentDto dtoToCreate = DepartmentDto.builder().name("Yeni Departman").description("Açıklama").build();
        DepartmentDto savedDto = DepartmentDto.builder().id(2L).name("Yeni Departman").description("Açıklama").build();

        given(departmentService.createDepartment(any(DepartmentDto.class))).willReturn(savedDto);

        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToCreate)))
                .andExpect(status().isCreated()) // 201
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Yeni Departman")));

        then(departmentService).should().createDepartment(any(DepartmentDto.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createDepartment_NameExists() throws Exception {
        DepartmentDto dtoToCreate = DepartmentDto.builder().name("Teknik Destek").build();
        given(departmentService.createDepartment(any(DepartmentDto.class)))
                .willThrow(new DepartmentNameExistsException("İsim zaten var"));

        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToCreate)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getDepartmentById() throws Exception {
        given(departmentService.getDepartmentById(1L)).willReturn(testDepartmentDto);

        mockMvc.perform(get("/api/departments/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Teknik Destek")));

        then(departmentService).should().getDepartmentById(1L);
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getDepartmentById_NotFound() throws Exception {
        given(departmentService.getDepartmentById(99L)).willThrow(new ResourceNotFoundException("Bulunamadı"));

        mockMvc.perform(get("/api/departments/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "AGENT")
    void getAllOrSearchDepartments() throws Exception {
        given(departmentService.getAllDepartments()).willReturn(List.of(testDepartmentDto));

        mockMvc.perform(get("/api/departments")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        then(departmentService).should().getAllDepartments();
        then(departmentService).should(never()).searchDepartmentsByName(anyString());

        reset(departmentService);

        given(departmentService.searchDepartmentsByName("Teknik")).willReturn(List.of(testDepartmentDto));

        mockMvc.perform(get("/api/departments")
                        .param("name", "Teknik")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        then(departmentService).should().searchDepartmentsByName("Teknik");
        then(departmentService).should(never()).getAllDepartments();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateDepartment() throws Exception {
        DepartmentDto dtoToUpdate = DepartmentDto.builder().name("Yeni Ad").description("Yeni Aciklama").build();
        DepartmentDto updatedDto = DepartmentDto.builder().id(1L).name("Yeni Ad").description("Yeni Aciklama").build();

        given(departmentService.updateDepartment(eq(1L), any(DepartmentDto.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/departments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Yeni Ad")));

        then(departmentService).should().updateDepartment(eq(1L), any(DepartmentDto.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteDepartment() throws Exception {
        willDoNothing().given(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(departmentService).should().deleteDepartment(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteDepartment_InUse() throws Exception {
        willThrow(new ResourceInUseException("Departman kullanılıyor"))
                .given(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isConflict());

        then(departmentService).should().deleteDepartment(1L);
    }
}
