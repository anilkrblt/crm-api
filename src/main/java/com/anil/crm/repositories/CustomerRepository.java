package com.anil.crm.repositories;

import com.anil.crm.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {


    @Query("SELECT c FROM Customer c WHERE c.user.email = :email")
    Optional<Customer> findCustomerByUserEmail(String email);


    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.user.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(c.user.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findCustomersByUserFirstNameContainingOrUserLastNameContaining(String name, String name2);
}