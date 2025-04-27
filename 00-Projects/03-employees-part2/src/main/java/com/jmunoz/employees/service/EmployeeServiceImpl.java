package com.jmunoz.employees.service;

import com.jmunoz.employees.dao.EmployeeRepository;
import com.jmunoz.employees.entity.Employee;
import com.jmunoz.employees.request.EmployeeRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository theEmployeeRepository) {
        this.employeeRepository = theEmployeeRepository;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(long theId) {
        return employeeRepository.findById(theId)
                .orElseThrow(() -> new RuntimeException("Did not find employee id - " + theId));
        
//        Optional<Employee> result = employeeRepository.findById(theId);
//
//        Employee theEmployee = null;
//
//        if (result.isPresent()) {
//            theEmployee = result.get();
//        } else {
//            throw new RuntimeException("Did not find employee id - " + theId);
//        }
//
//        return theEmployee;
    }

    @Transactional
    @Override
    public Employee save(EmployeeRequest employeeRequest) {
        Employee theEmployee = convertToEmployee(0, employeeRequest);
        return employeeRepository.save(theEmployee);
    }

    @Transactional
    @Override
    public Employee update(long id, EmployeeRequest employeeRequest) {
        Employee theEmployee = convertToEmployee(id, employeeRequest);
        return employeeRepository.save(theEmployee);
    }

    @Override
    public Employee convertToEmployee(long id, EmployeeRequest employeeRequest) {
        return new Employee(id,
                employeeRequest.getFirstName(),
                employeeRequest.getLastName(),
                employeeRequest.getEmail()
        );
    }

    @Transactional
    @Override
    public void deleteById(long theId) {
        employeeRepository.deleteById(theId);
    }
}
