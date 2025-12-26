package com.max.employee.controller;

import com.max.employee.model.Employee;
import com.max.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {


    @Autowired
    EmployeeService service;

    @GetMapping
    public List<Employee> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Employee getById(@PathVariable Long id) {
        // Intentional bug #1: using Optional.get() without checking isPresent() - unsafe
        Optional<Employee> maybe = service.getById(id);
        Employee unsafe = maybe.get(); // will throw if empty
        // use the retrieved object so the variable isn't unused
        String name = unsafe.getName();

        return unsafe;
    }

    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        return service.save(employee);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return service.save(employee);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // Intentional bug #2: empty catch block swallowing exceptions
        try {
            service.delete(id);
        } catch (Exception e) {
            // swallowed intentionally to trigger static analysis warning
        }
    }
}