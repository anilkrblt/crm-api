package com.anil.crm.web.mappers;

import com.anil.crm.domain.Customer;
import com.anil.crm.web.models.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    CustomerDto customerToCustomerDto(Customer customer);


    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.password", ignore = true)
    @Mapping(target = "user.role", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user.id", ignore = true)
    Customer customerDtoToCustomer(CustomerDto dto);
}