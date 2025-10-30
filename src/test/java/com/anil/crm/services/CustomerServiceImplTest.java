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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CustomerMapper customerMapper;

    @InjectMocks
    CustomerServiceImpl customerService;

    CustomerDto customerDto;
    Customer customer;
    User user;
    Long customerId = 1L;
    Long userId = 1L;
    String customerEmail = "test@customer.com";

    @BeforeEach
    void setUp() {
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
    void getAllCustomers() {
        given(customerRepository.findAll()).willReturn(List.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        List<CustomerDto> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customerDto.getId(), result.get(0).getId());
        then(customerRepository).should().findAll();
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    void getCustomerById() {
        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        CustomerDto foundDto = customerService.getCustomerById(customerId);

        assertNotNull(foundDto);
        assertEquals(customerId, foundDto.getId());
        assertEquals(customerEmail, foundDto.getEmail());
        then(customerRepository).should().findById(customerId);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    void getCustomerById_NotFound() {
        Long nonExistentId = 99L;
        given(customerRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(nonExistentId);
        });

        then(customerRepository).should().findById(nonExistentId);
        then(customerMapper).should(never()).customerToCustomerDto(any());
    }

    @Test
    void getCustomerByEmail() {
        given(customerRepository.findCustomerByUserEmail(customerEmail)).willReturn(Optional.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        CustomerDto foundDto = customerService.getCustomerByEmail(customerEmail);

        assertNotNull(foundDto);
        assertEquals(customerEmail, foundDto.getEmail());
        then(customerRepository).should().findCustomerByUserEmail(customerEmail);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    void getCustomerByEmail_NotFound() {
        String nonExistentEmail = "notfound@customer.com";
        given(customerRepository.findCustomerByUserEmail(nonExistentEmail)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerByEmail(nonExistentEmail);
        });
        then(customerRepository).should().findCustomerByUserEmail(nonExistentEmail);
        then(customerMapper).should(never()).customerToCustomerDto(any());
    }

    @Test
    void getCustomersByUserName() {
        String name = "Test";
        given(customerRepository.findCustomersByUserFirstNameContainingOrUserLastNameContaining(name, name))
                .willReturn(List.of(customer));
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        List<CustomerDto> result = customerService.getCustomersByUserName(name);

        assertNotNull(result);
        assertEquals(1, result.size());
        then(customerRepository).should().findCustomersByUserFirstNameContainingOrUserLastNameContaining(name, name);
        then(customerMapper).should().customerToCustomerDto(customer);
    }

    @Test
    void createCustomer() {
        CustomerDto dtoToSave = CustomerDto.builder()
                .email("new@customer.com")
                .firstName("New")
                .lastName("Customer")
                .password("password123")
                .phone("9876543210")
                .build();

        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dtoToSave.getPassword())).willReturn("hashedPassword");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);

        given(customerRepository.save(customerCaptor.capture())).willAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(2L);
            saved.getUser().setId(2L);
            return saved;
        });

        given(customerMapper.customerToCustomerDto(any(Customer.class))).willAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            return CustomerDto.builder()
                    .id(saved.getId())
                    .email(saved.getUser().getEmail())
                    .firstName(saved.getUser().getFirstName())
                    .lastName(saved.getUser().getLastName())
                    .phone(saved.getPhone())
                    .build();
        });

        CustomerDto savedDto = customerService.createCustomer(dtoToSave);

        assertNotNull(savedDto);
        assertEquals(2L, savedDto.getId());
        assertEquals(dtoToSave.getEmail(), savedDto.getEmail());

        Customer capturedCustomer = customerCaptor.getValue();
        assertEquals(dtoToSave.getPhone(), capturedCustomer.getPhone());
        assertEquals(dtoToSave.getFirstName(), capturedCustomer.getUser().getFirstName());
        assertEquals("hashedPassword", capturedCustomer.getUser().getPassword());
        assertEquals(Role.CUSTOMER, capturedCustomer.getUser().getRole());

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(passwordEncoder).should().encode(dtoToSave.getPassword());
        then(customerRepository).should().save(any(Customer.class));
        then(customerMapper).should().customerToCustomerDto(any(Customer.class));
    }

    @Test
    void createCustomer_EmailExists() {
        CustomerDto dtoToSave = CustomerDto.builder().email(customerEmail).password("pwd").build();
        given(userRepository.findByEmail(dtoToSave.getEmail())).willReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class, () -> customerService.createCustomer(dtoToSave));

        then(userRepository).should().findByEmail(dtoToSave.getEmail());
        then(passwordEncoder).should(never()).encode(anyString());
        then(customerRepository).should(never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer() {
        CustomerDto updatesDto = CustomerDto.builder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .email(customerEmail)
                .phone("1111111111")
                .build();

        given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
        given(customerRepository.save(any(Customer.class))).willReturn(customer);
        given(customerMapper.customerToCustomerDto(customer)).willReturn(customerDto);

        CustomerDto updatedDto = customerService.updateCustomer(customerId, updatesDto);

        assertNotNull(updatedDto);
        then(customerRepository).should().findById(customerId);
        then(customerRepository).should().save(customer);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        then(customerRepository).should().save(customerCaptor.capture());

        Customer capturedCustomer = customerCaptor.getValue();
        assertEquals("UpdatedFirst", capturedCustomer.getUser().getFirstName());
        assertEquals("UpdatedLast", capturedCustomer.getUser().getLastName());
        assertEquals("1111111111", capturedCustomer.getPhone());
    }

    @Test
    void deleteCustomerById() {
        given(customerRepository.existsById(customerId)).willReturn(true);
        given(ticketRepository.existsByCustomerId(customerId)).willReturn(false);
        willDoNothing().given(customerRepository).deleteById(customerId);

        assertDoesNotThrow(() -> customerService.deleteCustomerById(customerId));

        then(customerRepository).should().existsById(customerId);
        then(ticketRepository).should().existsByCustomerId(customerId);
        then(customerRepository).should().deleteById(customerId);
    }

    @Test
    void deleteCustomerById_InUse() {
        given(customerRepository.existsById(customerId)).willReturn(true);
        given(ticketRepository.existsByCustomerId(customerId)).willReturn(true);

        assertThrows(ResourceInUseException.class, () -> {
            customerService.deleteCustomerById(customerId);
        });

        then(customerRepository).should().existsById(customerId);
        then(ticketRepository).should().existsByCustomerId(customerId);
        then(customerRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deleteCustomerById_NotFound() {
        Long nonExistentId = 99L;
        given(customerRepository.existsById(nonExistentId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomerById(nonExistentId);
        });

        then(customerRepository).should().existsById(nonExistentId);
        then(ticketRepository).should(never()).existsByCustomerId(anyLong());
        then(customerRepository).should(never()).deleteById(anyLong());
    }
}
