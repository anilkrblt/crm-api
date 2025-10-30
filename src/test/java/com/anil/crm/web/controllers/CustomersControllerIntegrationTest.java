package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.ResourceInUseException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.services.CustomerService;
import com.anil.crm.services.JwtService;
import com.anil.crm.services.UserDetailsServiceImpl;
import com.anil.crm.web.models.CustomerDto;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomersController.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class})
class CustomersControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomerService customerService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    CustomerDto testCustomerDto;

    @BeforeEach
    void setUp() {
        testCustomerDto = CustomerDto.builder()
                .id(1L)
                .firstName("Test")
                .lastName("Customer")
                .email("test@customer.com")
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getCustomerById() throws Exception {
        given(customerService.getCustomerById(1L)).willReturn(testCustomerDto);

        mockMvc.perform(get("/api/customers/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is(testCustomerDto.getEmail())));

        then(customerService).should().getCustomerById(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getCustomerById_NotFound() throws Exception {
        given(customerService.getCustomerById(99L)).willThrow(new ResourceNotFoundException("Bulunamadı"));

        mockMvc.perform(get("/api/customers/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findCustomers_listAll() throws Exception {
        given(customerService.getAllCustomers()).willReturn(List.of(testCustomerDto));

        mockMvc.perform(get("/api/customers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        then(customerService).should().getAllCustomers();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findCustomers_filterByName() throws Exception {
        given(customerService.getCustomersByUserName("Test")).willReturn(List.of(testCustomerDto));

        mockMvc.perform(get("/api/customers")
                        .param("name", "Test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is("Test")));

        then(customerService).should().getCustomersByUserName("Test");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void findCustomers_filterByEmail() throws Exception {
        given(customerService.getCustomerByEmail(testCustomerDto.getEmail())).willReturn(testCustomerDto);

        mockMvc.perform(get("/api/customers")
                        .param("email", testCustomerDto.getEmail())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(testCustomerDto.getEmail())));

        then(customerService).should().getCustomerByEmail(testCustomerDto.getEmail());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCustomer() throws Exception {
        CustomerDto savedDto = CustomerDto.builder()
                .id(2L)
                .email("yeni@musteri.com")
                .firstName("Yeni")
                .lastName("Müşteri")
                .phone("+905551112233")
                .createdAt(LocalDateTime.now())
                .build();

        given(customerService.createCustomer(any(CustomerDto.class))).willReturn(savedDto);

        String requestBodyJson = """
        {
          "email": "yeni@musteri.com",
          "firstName": "Yeni",
          "lastName": "Müşteri",
          "password": "GuvenliSifre123!",
          "phone": "+905551112233"
        }
        """;

        mockMvc.perform(post("/api/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.email", is("yeni@musteri.com")));

        then(customerService).should().createCustomer(any(CustomerDto.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateCustomer() throws Exception {
        Long customerIdToUpdate = 1L;
        CustomerDto dtoToUpdate = CustomerDto.builder()
                .firstName("Güncel")
                .lastName("Müşteri")
                .email("guncel@musteri.com")
                .phone("5551112233")
                .build();

        CustomerDto updatedDto = CustomerDto.builder()
                .id(customerIdToUpdate)
                .firstName("Güncel")
                .lastName("Müşteri")
                .email("guncel@musteri.com")
                .phone("5551112233")
                .updatedAt(LocalDateTime.now())
                .build();

        given(customerService.updateCustomer(eq(customerIdToUpdate), any(CustomerDto.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/customers/{id}", customerIdToUpdate)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerIdToUpdate.intValue())))
                .andExpect(jsonPath("$.phone", is("5551112233")));

        then(customerService).should().updateCustomer(eq(customerIdToUpdate), any(CustomerDto.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteCustomer() throws Exception {
        willDoNothing().given(customerService).deleteCustomerById(1L);

        mockMvc.perform(delete("/api/customers/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        then(customerService).should().deleteCustomerById(1L);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteCustomer_Conflict() throws Exception {
        willThrow(new ResourceInUseException("Biletleri var"))
                .given(customerService).deleteCustomerById(1L);

        mockMvc.perform(delete("/api/customers/1")
                        .with(csrf()))
                .andExpect(status().isConflict());

        then(customerService).should().deleteCustomerById(1L);
    }
}

