package com.anil.crm.web.controllers;

import com.anil.crm.exceptions.ResourceNotFoundException; // Import your exception
import com.anil.crm.services.CustomerService;
import com.anil.crm.web.models.CustomerDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomersController.class) // Test only the CustomersController layer
@DisplayName("Customers Controller Unit Tests")
class CustomersControllerTest {

    @Autowired
    MockMvc mockMvc; // To perform HTTP requests

    @MockitoBean
    CustomerService customerService; // Mock the dependency

    @Autowired
    ObjectMapper objectMapper; // To convert objects to/from JSON

    CustomerDto validCustomerDto1;
    CustomerDto validCustomerDto2;

    @BeforeEach
    void setUp() {
        // Create sample DTOs used in multiple tests
        validCustomerDto1 = CustomerDto.builder()
                .id(1L)
                .firstName("Ali")
                .lastName("Veli")
                .email("ali.veli@email.com")
                .phone("+905551112233")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validCustomerDto2 = CustomerDto.builder()
                .id(2L)
                .firstName("Ayşe")
                .lastName("Kara")
                .email("ayse.kara@email.com")
                .phone("+905554445566")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Get Customer By ID - Success")
    void getCustomerById_shouldReturnCustomerDto_whenFound() throws Exception {
        // Given (Setup Mock)
        given(customerService.getCustomerById(validCustomerDto1.getId())).willReturn(validCustomerDto1);

        // When & Then (Perform Request & Assert Response)
        mockMvc.perform(get("/api/customers/{id}", validCustomerDto1.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validCustomerDto1.getId().intValue())))
                .andExpect(jsonPath("$.email", is(validCustomerDto1.getEmail())));

        // Verify that the service method was called exactly once
        then(customerService).should(times(1)).getCustomerById(validCustomerDto1.getId());
    }

    @Test
    @DisplayName("Get Customer By ID - Not Found")
    void getCustomerById_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        given(customerService.getCustomerById(nonExistentId)).willThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(customerService).should(times(1)).getCustomerById(nonExistentId);
    }


    @Test
    @DisplayName("Get Customer By Email - Success")
    void getCustomerByEmail_shouldReturnCustomerDto_whenFound() throws Exception {
        // Given
        given(customerService.getCustomerByEmail(validCustomerDto1.getEmail())).willReturn(validCustomerDto1);

        // When & Then
        mockMvc.perform(get("/api/customers/email")
                        .param("email", validCustomerDto1.getEmail())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(validCustomerDto1.getId().intValue())))
                .andExpect(jsonPath("$.email", is(validCustomerDto1.getEmail())));

        then(customerService).should(times(1)).getCustomerByEmail(validCustomerDto1.getEmail());
    }

    @Test
    @DisplayName("Get Customer By Email - Not Found")
    void getCustomerByEmail_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        String nonExistentEmail = "notfound@email.com";
        given(customerService.getCustomerByEmail(nonExistentEmail)).willThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/customers/email")
                        .param("email", nonExistentEmail)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(customerService).should(times(1)).getCustomerByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Get All Customers - Success")
    void getAllCustomers_shouldReturnListOfCustomers() throws Exception {
        // Given
        List<CustomerDto> customerList = Arrays.asList(validCustomerDto1, validCustomerDto2);
        given(customerService.getAllCustomers()).willReturn(customerList);

        // When & Then
        mockMvc.perform(get("/api/customers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // Check if the list size is 2
                .andExpect(jsonPath("$[0].id", is(validCustomerDto1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(validCustomerDto2.getId().intValue())));

        then(customerService).should(times(1)).getAllCustomers();
    }

    @Test
    @DisplayName("Search Customers By Name - Success")
    void searchCustomersByName_shouldReturnMatchingCustomers() throws Exception {
        // Given
        String searchName = "Veli";
        List<CustomerDto> resultList = List.of(validCustomerDto1);
        given(customerService.getCustomersByUserName(searchName)).willReturn(resultList);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                        .param("name", searchName) // Add query parameter
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lastName", is("Veli")));

        then(customerService).should(times(1)).getCustomersByUserName(searchName);
    }


    @Test
    @DisplayName("Create Customer - Success")
    void createCustomer_shouldReturnCreatedCustomer_whenValid() throws Exception {
        // Given
        CustomerDto customerToCreate = CustomerDto.builder() // DTO without ID
                .firstName("Fatma")
                .lastName("Yıldız")
                .email("fatma.yildiz@email.com")
                .phone("+905551234567")
                .password("CokGucluSifre1.")
                .build();

        CustomerDto savedCustomer = CustomerDto.builder() // DTO returned by service (with ID)
                .id(3L)
                .firstName("Fatma")
                .lastName("Yıldız")
                .email("fatma.yildiz@email.com")
                .phone("+905551234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(customerService.createCustomer(any(CustomerDto.class))).willReturn(savedCustomer);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToCreate))) // Send DTO as JSON
                .andExpect(status().isCreated()) // Expect 201 Created
                .andExpect(header().exists("Location")) // Expect Location header
                .andExpect(jsonPath("$.id", is(savedCustomer.getId().intValue())))
                .andExpect(jsonPath("$.email", is(savedCustomer.getEmail())));

        then(customerService).should(times(1)).createCustomer(any(CustomerDto.class));
    }

    @Test
    @DisplayName("Create Customer - Validation Failure")
    void createCustomer_shouldReturnBadRequest_whenInvalid() throws Exception {
        // Given
        CustomerDto invalidCustomer = CustomerDto.builder() // Missing required fields
                .email("invalid-email") // Invalid email format
                .phone("123") // Invalid phone format
                .build();

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request

        // Service method should NOT be called if validation fails
        then(customerService).should(never()).createCustomer(any(CustomerDto.class));
    }


    @Test
    @DisplayName("Update Customer - Success")
    void updateCustomer_shouldReturnUpdatedCustomer_whenValid() throws Exception {
        // Given
        Long customerId = validCustomerDto1.getId();
        CustomerDto customerUpdates = CustomerDto.builder()
                .firstName("Ali")
                .lastName("Veli Updated") // Last name updated
                .email("ali.veli@email.com")
                .phone("+905559998877") // Phone updated
                .build();

        CustomerDto updatedCustomerFromService = CustomerDto.builder()
                .id(customerId)
                .firstName("Ali")
                .lastName("Veli Updated")
                .email("ali.veli@email.com")
                .phone("+905559998877")
                .createdAt(validCustomerDto1.getCreatedAt()) // Should remain the same
                .updatedAt(LocalDateTime.now()) // Should be updated
                .build();

        given(customerService.updateCustomer(eq(customerId), any(CustomerDto.class))).willReturn(updatedCustomerFromService);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerUpdates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customerId.intValue())))
                .andExpect(jsonPath("$.lastName", is(updatedCustomerFromService.getLastName())))
                .andExpect(jsonPath("$.phone", is(updatedCustomerFromService.getPhone())));

        then(customerService).should(times(1)).updateCustomer(eq(customerId), any(CustomerDto.class));
    }

    @Test
    @DisplayName("Update Customer - Not Found")
    void updateCustomer_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        CustomerDto customerUpdates = CustomerDto.builder() /* valid update data */ .build();
        given(customerService.updateCustomer(eq(nonExistentId), any(CustomerDto.class)))
                .willThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerUpdates)))
                .andExpect(status().isNotFound());

        then(customerService).should(times(1)).updateCustomer(eq(nonExistentId), any(CustomerDto.class));
    }

    @Test
    @DisplayName("Delete Customer - Success")
    void deleteCustomer_shouldReturnNoContent_whenSuccess() throws Exception {
        // Given
        Long customerIdToDelete = 1L;
        // Mock void method: do nothing when called
        willDoNothing().given(customerService).deleteCustomerById(customerIdToDelete);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", customerIdToDelete)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); // Expect 204 No Content

        then(customerService).should(times(1)).deleteCustomerById(customerIdToDelete);
    }

    @Test
    @DisplayName("Delete Customer - Not Found")
    void deleteCustomer_shouldReturnNotFound_whenNotFound() throws Exception {
        // Given
        Long nonExistentId = 99L;
        willThrow(new ResourceNotFoundException("Customer not found"))
                .given(customerService).deleteCustomerById(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", nonExistentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        then(customerService).should(times(1)).deleteCustomerById(nonExistentId);
    }
}