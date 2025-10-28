package com.anil.crm.services;

import com.anil.crm.domain.Customer;
import com.anil.crm.domain.Role;
import com.anil.crm.domain.User;
import com.anil.crm.exceptions.EmailAlreadyExistsException;
import com.anil.crm.exceptions.ResourceInUseException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.CustomerRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.repositories.UserRepository;
import com.anil.crm.web.mappers.CustomerMapper;
import com.anil.crm.web.models.CustomerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class) // Enable Mockito integration
@DisplayName("Customer Service Implementation Unit Tests")
class CustomerServiceImplTest {

    @Mock // Create mocks for dependencies
    TicketRepository ticketRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CustomerMapper customerMapper;

    @InjectMocks // Inject mocks into the class under test
    CustomerServiceImpl customerService;

    // Test data
    CustomerDto customerDto;
    Customer customer;
    User user;
    Long customerId = 1L;
    Long userId = 1L;
    String customerEmail = "test@customer.com";

    @BeforeEach
    void setUp() {
        // Initialize test data before each test
        user = User.builder()
                .id(userId)
                .email(customerEmail)
                .firstName("Test")
                .lastName("Customer")
                .role(Role.CUSTOMER)
                .password("hashedPassword")
                .build();
        customer = Customer.builder()
                .id(customerId)
                .phone("1234567890")
                .user(user)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        customerDto = CustomerDto.builder()
                .id(customerId)
                .email(customerEmail)
                .firstName("Test")
                .lastName("Customer")
                .phone("1234567890")
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Get All Customers - Success")
    void getAllCustomers_shouldReturnListOfCustomerDtos() {
        // Given: Mock repository and mapper behavior
        given(customerRepository.findAll()).willReturn(List.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        // When: Call the service method
        List<CustomerDto> result = customerService.getAllCustomers();

        // Then: Assert the results and verify interactions
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customerDto.getId(), result.get(0).getId());
        then(customerRepository).should().findAll();
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    @DisplayName("Get Customer By ID - Success")
    void getCustomerById_shouldReturnCustomerDto_whenFound() {
        // Given
        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        // When
        CustomerDto foundDto = customerService.getCustomerById(customerId);

        // Then
        assertNotNull(foundDto);
        assertEquals(customerId, foundDto.getId());
        assertEquals(customerEmail, foundDto.getEmail());
        then(customerRepository).should().findById(customerId);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    @DisplayName("Get Customer By ID - Not Found")
    void getCustomerById_shouldThrowResourceNotFoundException_whenNotFound() {
        // Given
        Long nonExistentId = 99L;
        given(customerRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById(nonExistentId));
        then(customerRepository).should().findById(nonExistentId);
        then(customerMapper).should(never()).customerToCustomerDto(any()); // Mapper should not be called
    }

    @Test
    @DisplayName("Get Customer By Email - Success")
    void getCustomerByEmail_shouldReturnCustomerDto_whenFound() {
        // Given
        given(customerRepository.findCustomerByUserEmail(customerEmail)).willReturn(Optional.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        // When
        CustomerDto foundDto = customerService.getCustomerByEmail(customerEmail);

        // Then
        assertNotNull(foundDto);
        assertEquals(customerEmail, foundDto.getEmail());
        then(customerRepository).should().findCustomerByUserEmail(customerEmail);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    @DisplayName("Get Customer By Email - Not Found")
    void getCustomerByEmail_shouldThrowResourceNotFoundException_whenNotFound() {
        // Given
        String nonExistentEmail = "notfound@customer.com";
        given(customerRepository.findCustomerByUserEmail(nonExistentEmail)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerByEmail(nonExistentEmail));
        then(customerRepository).should().findCustomerByUserEmail(nonExistentEmail);
        then(customerMapper).should(never()).customerToCustomerDto(any());
    }

    @Test
    @DisplayName("Get Customers By User Name - Success")
    void getCustomersByUserName_shouldReturnListOfCustomerDtos() {
        // Given
        String name = "Test";
        given(customerRepository.findCustomersByUserFirstNameContainingOrUserLastNameContaining(name, name))
                .willReturn(List.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        // When
        List<CustomerDto> result = customerService.getCustomersByUserName(name);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        then(customerRepository).should().findCustomersByUserFirstNameContainingOrUserLastNameContaining(name, name);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    @DisplayName("Create Customer - Success")
    void createCustomer_shouldSaveAndReturnCustomer() {
        // Given
        CustomerDto dtoToSave = CustomerDto.builder()
                .email("new@customer.com")
                .firstName("New")
                .lastName("Customer")
                .password("password123")
                .phone("9876543210")
                .build();

        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.empty()); // Email is available
        given(passwordEncoder.encode(dtoToSave.getPassword())).willReturn("hashedPassword"); // Mock hashing
        // Mock the save to return an entity with IDs
        given(customerRepository.save(any(Customer.class))).willAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.getUser().setId(2L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });
        // Mock the mapper to return a DTO based on the saved entity
        given(customerMapper.customerToCustomerDto(any(Customer.class))).willAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            return CustomerDto.builder()
                    .id(saved.getId())
                    .email(saved.getUser().getEmail())
                    .firstName(saved.getUser().getFirstName())
                    .lastName(saved.getUser().getLastName())
                    .phone(saved.getPhone())
                    .createdAt(saved.getCreatedAt())
                    .updatedAt(saved.getUpdatedAt())
                    .build();
        });

        // When
        CustomerDto savedDto = customerService.createCustomer(dtoToSave);

        // Then
        assertNotNull(savedDto);
        assertEquals(2L, savedDto.getId());
        assertEquals(dtoToSave.getEmail(), savedDto.getEmail());
        assertEquals(dtoToSave.getPhone(), savedDto.getPhone());
        assertNotNull(savedDto.getCreatedAt());

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(passwordEncoder).should().encode(dtoToSave.getPassword());
        then(customerRepository).should().save(any(Customer.class));
        then(customerMapper).should().customerToCustomerDto(any(Customer.class));
    }

    @Test
    @DisplayName("Create Customer - Email Exists")
    void createCustomer_shouldThrowEmailExistsException_whenEmailInUse() {
        // Given
        CustomerDto dtoToSave = CustomerDto.builder().email(customerEmail).password("pwd").build();
        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.of(user)); // Email exists

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> customerService.createCustomer(dtoToSave));

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        // Verify no save attempts were made
        then(passwordEncoder).should(never()).encode(anyString());
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Update Customer - Success")
    void updateCustomer_shouldUpdateAndReturnCustomer() {
        // Given
        CustomerDto updatesDto = CustomerDto.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .email("updated.customer@test.com") // New email
                .phone("0000000000") // New phone
                .build();

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(userRepository.findByEmail(updatesDto.getEmail())).willReturn(Optional.empty()); // New email is available
        given(customerRepository.save(any(Customer.class))).willAnswer(invocation -> invocation.getArgument(0)); // Return updated entity
        // Mock mapper to return DTO based on updated entity
        given(customerMapper.customerToCustomerDto(any(Customer.class))).willAnswer(invocation -> {
            Customer updated = invocation.getArgument(0);
            return CustomerDto.builder()
                    .id(updated.getId())
                    .email(updated.getUser().getEmail()) // Updated email
                    .firstName(updated.getUser().getFirstName()) // Updated name
                    .lastName(updated.getUser().getLastName())
                    .phone(updated.getPhone()) // Updated phone
                    .createdAt(updated.getCreatedAt())
                    .updatedAt(updated.getUpdatedAt()) // Should be updated now
                    .build();
        });

        // When
        CustomerDto updatedDto = customerService.updateCustomer(customerId, updatesDto);

        // Then
        assertNotNull(updatedDto);
        assertEquals(customerId, updatedDto.getId());
        assertEquals(updatesDto.getEmail(), updatedDto.getEmail());
        assertEquals(updatesDto.getFirstName(), updatedDto.getFirstName());
        assertEquals(updatesDto.getPhone(), updatedDto.getPhone());
        assertNotEquals(customer.getUpdatedAt(), updatedDto.getUpdatedAt()); // Check timestamp update

        then(customerRepository).should().findById(customerId);
        then(userRepository).should().findByEmail(updatesDto.getEmail());
        then(customerRepository).should().save(any(Customer.class));
        then(customerMapper).should().customerToCustomerDto(any(Customer.class));
    }

    @Test
    @DisplayName("Update Customer - Not Found")
    void updateCustomer_shouldThrowNotFoundException_whenCustomerNotFound() {
        // Given
        Long nonExistentId = 99L;
        CustomerDto updatesDto = CustomerDto.builder().email("any@email.com").build();
        given(customerRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> customerService.updateCustomer(nonExistentId, updatesDto));
        then(customerRepository).should().findById(nonExistentId);
        then(userRepository).should(never()).findByEmail(anyString());
        then(customerRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("Update Customer - Email Exists")
    void updateCustomer_shouldThrowEmailExistsException_whenEmailTaken() {
        // Given
        CustomerDto updatesDto = CustomerDto.builder()
                .email("existing.other@test.com") // Email that belongs to another user
                .firstName("Test").lastName("Customer").phone("123") // Include required fields
                .build();
        User otherUser = User.builder().id(2L).email("existing.other@test.com").build();

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        // Mock userRepository to find another user with the target email
        given(userRepository.findByEmail(updatesDto.getEmail())).willReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> customerService.updateCustomer(customerId, updatesDto));

        then(customerRepository).should().findById(customerId);
        then(userRepository).should().findByEmail(updatesDto.getEmail());
        then(customerRepository).should(never()).save(any()); // Should not save
    }

    @Test
    @DisplayName("Delete Customer By ID - Success")
    void deleteCustomerById_shouldCallRepositoryDelete_whenExistsAndNoTickets() {
        // Given
        given(customerRepository.existsById(customerId)).willReturn(true);
        given(ticketRepository.existsByCustomerId(customerId)).willReturn(false); // No tickets exist
        willDoNothing().given(customerRepository).deleteById(customerId); // Mock void method

        // When
        customerService.deleteCustomerById(customerId);

        // Then
        then(customerRepository).should().existsById(customerId);
        then(ticketRepository).should().existsByCustomerId(customerId);
        then(customerRepository).should().deleteById(customerId);
    }

    @Test
    @DisplayName("Delete Customer By ID - Not Found")
    void deleteCustomerById_shouldThrowNotFoundException_whenNotExists() {
        // Given
        Long nonExistentId = 99L;
        given(customerRepository.existsById(nonExistentId)).willReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomerById(nonExistentId));

        then(customerRepository).should().existsById(nonExistentId);
        then(ticketRepository).should(never()).existsByCustomerId(anyLong()); // Ticket check shouldn't happen
        then(customerRepository).should(never()).deleteById(anyLong()); // Delete should not be called
    }

    @Test
    @DisplayName("Delete Customer By ID - Tickets Exist")
    void deleteCustomerById_shouldThrowResourceInUseException_whenTicketsExist() {
        // Given
        given(customerRepository.existsById(customerId)).willReturn(true);
        given(ticketRepository.existsByCustomerId(customerId)).willReturn(true); // Tickets EXIST

        // When & Then
        assertThrows(ResourceInUseException.class, () -> customerService.deleteCustomerById(customerId));

        then(customerRepository).should().existsById(customerId);
        then(ticketRepository).should().existsByCustomerId(customerId);
        then(customerRepository).should(never()).deleteById(anyLong()); // Delete should not be called
    }
}