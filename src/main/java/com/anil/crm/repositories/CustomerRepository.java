package com.anil.crm.repositories;

import com.anil.crm.domain.Customer;
import com.anil.crm.web.models.CustomerDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findCustomerByEmail(String email);

    List<Customer> findCustomersByFullName(String fullName);
}
