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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;
    private final AgentRepository agentRepository;
    private final DepartmentMapper departmentMapper;
    private final TicketRepository ticketRepository;


    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        log.info("Attempting to create new department with name: {}", departmentDto.getName());

        if (departmentRepository.findByName(departmentDto.getName()).isPresent()) {
            log.warn("Department name already exists: {}", departmentDto.getName());
            throw new DepartmentNameExistsException("Department name already in use: " + departmentDto.getName());
        }

        Department department = departmentMapper.departmentDtoToDepartment(departmentDto);
        department.setId(null);

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with id: {} and name: {}", savedDepartment.getId(), savedDepartment.getName());

        return departmentMapper.departmentToDepartmentDto(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        log.debug("Fetching all departments");
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        log.debug("Fetching department by id: {}", id);
        return departmentRepository.findById(id)
                .map(departmentMapper::departmentToDepartmentDto)
                .orElseThrow(() -> {
                    log.warn("Department not found with id: {}", id);
                    return new ResourceNotFoundException("Department not found with id: " + id);
                });
    }

    @Override
    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        log.info("Attempting to update department with id: {}", id);

        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (departmentDto.getName() != null && !existingDepartment.getName().equalsIgnoreCase(departmentDto.getName())) {
            if (departmentRepository.findByName(departmentDto.getName()).isPresent()) {
                log.warn("Update failed. New department name '{}' already exists.", departmentDto.getName());
                throw new DepartmentNameExistsException("Department name already in use: " + departmentDto.getName());
            }
            existingDepartment.setName(departmentDto.getName());
        }

        existingDepartment.setDescription(departmentDto.getDescription());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("Department updated successfully for id: {}", updatedDepartment.getId());

        return departmentMapper.departmentToDepartmentDto(updatedDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Attempting to delete department with id: {}", id);

        if (!departmentRepository.existsById(id)) {
            log.warn("Failed to delete. Department not found with id: {}", id);
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }

        if (agentRepository.existsByDepartmentId(id)) {
            log.warn("Failed to delete department {}. Department is in use by agents.", id);
            throw new ResourceInUseException("Cannot delete department. Agents are still assigned to it.");
        }

        if (ticketRepository.existsByDepartmentId(id)) {
            log.warn("Failed to delete department {}. Department has associated tickets.", id);
            throw new ResourceInUseException("Cannot delete department. Tickets are still assigned to it.");
        }

        departmentRepository.deleteById(id);
        log.info("Department deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> searchDepartmentsByName(String name) {
        log.debug("Searching departments with name containing: {}", name);

        List<Department> foundDepartments = departmentRepository.findByNameContainingIgnoreCase(name);

        return foundDepartments.stream()
                .map(departmentMapper::departmentToDepartmentDto)
                .collect(Collectors.toList());
    }
}