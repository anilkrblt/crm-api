package com.anil.crm.web.mappers;

import com.anil.crm.domain.Customer;
import com.anil.crm.web.models.CustomerDto;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer(CustomerDto dto);

    CustomerDto customerToCustomerDto(Customer customer);
}
