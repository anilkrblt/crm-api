package com.anil.crm.services;

import com.anil.crm.web.models.CustomerDto;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    Optional<CustomerDto> getCustomerById(Long id);

    Optional<CustomerDto> getCustomerByEmail(String email);

    List<CustomerDto> getAllCustomers();

    CustomerDto saveCustomer(CustomerDto customerDto);

    CustomerDto updateCustomer(CustomerDto customerDto);

    void deleteCustomerById(Long id);

    // opsiyonel: filtreleme
    List<CustomerDto> getCustomersByName(String name);
}
