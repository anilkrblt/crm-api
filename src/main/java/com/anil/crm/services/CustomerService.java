package com.anil.crm.services;

import com.anil.crm.web.models.CustomerDto;

import java.util.List;


public interface CustomerService {

    List<CustomerDto> getAllCustomers();


    CustomerDto getCustomerById(Long id);


    CustomerDto getCustomerByEmail(String email);


    List<CustomerDto> getCustomersByUserName(String name);


    CustomerDto createCustomer(CustomerDto customerDto);


    CustomerDto updateCustomer(Long id, CustomerDto customerDto);


    void deleteCustomerById(Long id);
}