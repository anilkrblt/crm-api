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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.debug("Fetching all customers");
        return customerRepository
                .findAll()
                .stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        log.debug("Fetching customer by id: {}", id);
        return customerRepository
                .findById(id)
                .map(customerMapper::customerToCustomerDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.debug("Fetching customer by email: {}", email);
        return customerRepository
                .findCustomerByUserEmail(email)
                .map(customerMapper::customerToCustomerDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByUserName(String name) {
        log.debug("Searching customers by name: {}", name);
        return customerRepository
                .findCustomersByUserFirstNameContainingOrUserLastNameContaining(name, name)
                .stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        log.info("Creating new customer with email: {}", customerDto.getEmail());

        if (userRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            log.warn("Email already exists: {}", customerDto.getEmail());
            throw new EmailAlreadyExistsException("Email already in use: " + customerDto.getEmail());
        }

        User user = new User();
        user.setFirstName(customerDto.getFirstName());
        user.setLastName(customerDto.getLastName());
        user.setEmail(customerDto.getEmail());
        user.setPassword(passwordEncoder.encode(customerDto.getPassword()));
        user.setRole(Role.CUSTOMER);

        Customer customer = new Customer();
        customer.setPhone(customerDto.getPhone());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setUser(user);

        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created with id: {} and user id: {}", savedCustomer.getId(), savedCustomer.getUser().getId());

        return customerMapper.customerToCustomerDto(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        log.info("Updating customer with id: {}", id);

        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        User existingUser = existingCustomer.getUser();

        existingCustomer.setPhone(customerDto.getPhone());
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        existingUser.setFirstName(customerDto.getFirstName());
        existingUser.setLastName(customerDto.getLastName());

        if (!existingUser.getEmail().equals(customerDto.getEmail())) {
            if (userRepository.findByEmail(customerDto.getEmail()).isPresent()) {
                log.warn("Email update failed. Email already exists: {}", customerDto.getEmail());
                throw new EmailAlreadyExistsException("Email already in use: " + customerDto.getEmail());
            }
            existingUser.setEmail(customerDto.getEmail());
        }
        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return customerMapper.customerToCustomerDto(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomerById(Long id) {
        log.info("Attempting to delete customer with id: {}", id);

        if (!customerRepository.existsById(id)) {
            log.warn("Failed to delete. Customer not found with id: {}", id);
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }

        if (ticketRepository.existsByCustomerId(id)) {
            log.warn("Failed to delete customer {}. Customer has associated tickets.", id);
            throw new ResourceInUseException("Bu müşteri silinemez. Müşteriye ait aktif biletler bulunmaktadır.");
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with id: {}", id);
    }
}