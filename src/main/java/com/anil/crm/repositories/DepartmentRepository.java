package com.anil.crm.repositories;

import com.anil.crm.domain.Department;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(@NotBlank(message = "Departman adı boş olamaz") String name);

    List<Department> findByNameContainingIgnoreCase(String name);
}
