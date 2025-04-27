package com.jmunoz.employees.dao;

import com.jmunoz.employees.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // ¡Y ya está! No es necesario escribir más código
}
