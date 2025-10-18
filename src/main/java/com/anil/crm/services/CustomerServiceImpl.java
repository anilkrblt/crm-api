package com.anil.crm.services;

import com.anil.crm.domain.Customer;
import com.anil.crm.repositories.CustomerRepository;
import com.anil.crm.web.mappers.CustomerMapper;
import com.anil.crm.web.models.CustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDto> getCustomerById(Long id) {
        return customerRepository
                .findById(id)
                .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public Optional<CustomerDto> getCustomerByEmail(String email) {
        return customerRepository
                .findCustomerByEmail(email)
                .map(customerMapper::customerToCustomerDto);
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerRepository
                .findAll()
                .stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        return customerMapper.customerToCustomerDto(customerRepository.save(customerMapper.customerDtoToCustomer(customerDto)));
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        Customer customer = customerRepository.findById(customerDto.getId()).orElse(null);
        if (customer != null) {
            customer.setUpdatedAt(LocalDateTime.now());
            customer.setEmail(customerDto.getEmail());
            customer.setFullName(customerDto.getFullName());
            //todo hash before save
            customer.setPasswordHash(customerDto.getPasswordHash());
            customer.setPhone(customerDto.getPhone());
            customerRepository.save(customer);
        }
        return customerMapper.customerToCustomerDto(customer);
    }

    @Override
    public void deleteCustomerById(Long id) {
        customerRepository.deleteById(id);

    }

    @Override
    public List<CustomerDto> getCustomersByName(String name) {
        return customerRepository
                .findCustomersByFullName(name)
                .stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }
}
