package com.anil.crm.services;

import com.anil.crm.domain.Department;
import com.anil.crm.exceptions.DepartmentNameExistsException;
import com.anil.crm.exceptions.ResourceInUseException;
import com.anil.crm.exceptions.ResourceNotFoundException;
import com.anil.crm.repositories.AgentRepository;
import com.anil.crm.repositories.DepartmentRepository;
import com.anil.crm.repositories.TicketRepository;
import com.anil.crm.web.mappers.DepartmentMapper;
import com.anil.crm.web.models.DepartmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    DepartmentRepository departmentRepository;
    @Mock
    AgentRepository agentRepository;
    @Mock
    TicketRepository ticketRepository;
    @Mock
    DepartmentMapper departmentMapper;

    @InjectMocks
    DepartmentServiceImpl departmentService;

    Department department1;
    DepartmentDto departmentDto1;
    Long deptId = 1L;
    String deptName = "Teknik Destek";

    @BeforeEach
    void setUp() {
        department1 = Department.builder()
                .id(deptId)
                .name(deptName)
                .description("Test departmanı")
                .build();

        departmentDto1 = DepartmentDto.builder()
                .id(deptId)
                .name(deptName)
                .description("Test departmanı")
                .build();
    }

    @Test
    void createDepartment() {
        DepartmentDto dtoToSave = DepartmentDto.builder().name(deptName).description("Yeni").build();
        Department transientDept = Department.builder().name(deptName).description("Yeni").build();
        Department savedDept = Department.builder().id(deptId).name(deptName).description("Yeni").build();

        given(departmentRepository.findByName(dtoToSave.getName())).willReturn(Optional.empty());
        given(departmentMapper.departmentDtoToDepartment(dtoToSave)).willReturn(transientDept);
        given(departmentRepository.save(transientDept)).willReturn(savedDept);
        given(departmentMapper.departmentToDepartmentDto(savedDept)).willReturn(departmentDto1);

        DepartmentDto resultDto = departmentService.createDepartment(dtoToSave);

        assertNotNull(resultDto);
        assertEquals(deptName, resultDto.getName());
        assertEquals(deptId, resultDto.getId());
        then(departmentRepository).should().findByName(deptName);
        then(departmentRepository).should().save(transientDept);
        then(departmentMapper).should().departmentToDepartmentDto(savedDept);
    }

    @Test
    void createDepartment_NameExists() {
        DepartmentDto dtoToSave = DepartmentDto.builder().name(deptName).build();
        given(departmentRepository.findByName(deptName)).willReturn(Optional.of(department1));

        assertThrows(DepartmentNameExistsException.class, () -> {
            departmentService.createDepartment(dtoToSave);
        });

        then(departmentRepository).should().findByName(deptName);
        then(departmentRepository).should(never()).save(any());
    }

    @Test
    void getAllDepartments() {
        given(departmentRepository.findAll()).willReturn(List.of(department1));
        given(departmentMapper.departmentToDepartmentDto(department1)).willReturn(departmentDto1);

        List<DepartmentDto> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(deptName, result.get(0).getName());
        then(departmentRepository).should().findAll();
    }

    @Test
    void getDepartmentById() {
        given(departmentRepository.findById(deptId)).willReturn(Optional.of(department1));
        given(departmentMapper.departmentToDepartmentDto(department1)).willReturn(departmentDto1);

        DepartmentDto result = departmentService.getDepartmentById(deptId);

        assertNotNull(result);
        assertEquals(deptId, result.getId());
        then(departmentRepository).should().findById(deptId);
    }

    @Test
    void getDepartmentById_NotFound() {
        Long nonExistentId = 99L;
        given(departmentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.getDepartmentById(nonExistentId);
        });
        then(departmentMapper).should(never()).departmentToDepartmentDto(any());
    }

    @Test
    void updateDepartment_NameExists() {
        String newName = "Dolu Departman Adı";
        DepartmentDto updatesDto = DepartmentDto.builder().name(newName).build();
        Department existingOtherDept = Department.builder().id(2L).name(newName).build();

        given(departmentRepository.findById(deptId)).willReturn(Optional.of(department1));
        given(departmentRepository.findByName(newName)).willReturn(Optional.of(existingOtherDept));

        assertThrows(DepartmentNameExistsException.class, () -> {
            departmentService.updateDepartment(deptId, updatesDto);
        });

        then(departmentRepository).should().findById(deptId);
        then(departmentRepository).should().findByName(newName);
        then(departmentRepository).should(never()).save(any());
    }

    @Test
    void deleteDepartment_Success() {
        given(departmentRepository.existsById(deptId)).willReturn(true);
        given(agentRepository.existsByDepartmentId(deptId)).willReturn(false);
        given(ticketRepository.existsByDepartmentId(deptId)).willReturn(false);
        willDoNothing().given(departmentRepository).deleteById(deptId);
        assertDoesNotThrow(() -> departmentService.deleteDepartment(deptId));

        then(departmentRepository).should().existsById(deptId);
        then(agentRepository).should().existsByDepartmentId(deptId);
        then(ticketRepository).should().existsByDepartmentId(deptId);
        then(departmentRepository).should().deleteById(deptId);
    }

    @Test
    void deleteDepartment_NotFound() {
        Long nonExistentId = 99L;
        given(departmentRepository.existsById(nonExistentId)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            departmentService.deleteDepartment(nonExistentId);
        });

        then(departmentRepository).should().existsById(nonExistentId);
        then(agentRepository).should(never()).existsByDepartmentId(anyLong());
        then(ticketRepository).should(never()).existsByDepartmentId(anyLong());
        then(departmentRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deleteDepartment_InUseByAgents() {
        given(departmentRepository.existsById(deptId)).willReturn(true);
        given(agentRepository.existsByDepartmentId(deptId)).willReturn(true);

        assertThrows(ResourceInUseException.class, () -> {
            departmentService.deleteDepartment(deptId);
        });

        then(departmentRepository).should().existsById(deptId);
        then(agentRepository).should().existsByDepartmentId(deptId);
        then(departmentRepository).should(never()).deleteById(anyLong());
    }

    @Test
    void deleteDepartment_InUseByTickets() {
        given(departmentRepository.existsById(deptId)).willReturn(true);
        given(agentRepository.existsByDepartmentId(deptId)).willReturn(false);
        given(ticketRepository.existsByDepartmentId(deptId)).willReturn(true);

        assertThrows(ResourceInUseException.class, () -> {
            departmentService.deleteDepartment(deptId);
        });

        then(departmentRepository).should().existsById(deptId);
        then(agentRepository).should().existsByDepartmentId(deptId);
        then(ticketRepository).should().existsByDepartmentId(deptId);
        then(departmentRepository).should(never()).deleteById(anyLong());
    }

}
