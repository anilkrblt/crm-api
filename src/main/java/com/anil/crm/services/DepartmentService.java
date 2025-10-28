package com.anil.crm.services;

import com.anil.crm.web.models.DepartmentDto;

import java.util.List;


public interface DepartmentService {


    DepartmentDto createDepartment(DepartmentDto departmentDto);


    List<DepartmentDto> getAllDepartments();


    DepartmentDto getDepartmentById(Long id);


    DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto);

    void deleteDepartment(Long id);

    List<DepartmentDto> searchDepartmentsByName(String name);
}