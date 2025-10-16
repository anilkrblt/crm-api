package com.anil.crm.services;

import com.anil.crm.repositories.CustomerRepository;
import com.anil.crm.web.mappers.CustomerMapper;
import com.anil.crm.web.models.CustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDto> getCustomerById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<CustomerDto> getCustomerByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public List<CustomerDto> getAllCustomers() {
        return List.of();
    }

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        return null;
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        return null;
    }

    @Override
    public void deleteCustomerById(Long id) {

    }

    @Override
    public List<CustomerDto> getCustomersByName(String name) {
        return List.of();
    }
}
