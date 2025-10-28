package com.anil.crm.web.mappers;

import com.anil.crm.domain.Department;
import com.anil.crm.web.models.DepartmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper
public interface DepartmentMapper {


    Department departmentDtoToDepartment(DepartmentDto departmentDto);

    DepartmentDto departmentToDepartmentDto(Department savedDepartment);
}
