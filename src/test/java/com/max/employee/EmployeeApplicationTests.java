package com.max.employee;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EmployeeApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void calculatorAddsTwoNumbers() {
		EmployeeApplication app = new EmployeeApplication();
		assertEquals(5, app.calculator(2, 3));
	}

	@Test
	void calculatorHandlesNegativeNumbers() {
		EmployeeApplication app = new EmployeeApplication();
		assertEquals(-1, app.calculator(2, -3));
	}

	@Test
	void calculatorWithZero() {
		EmployeeApplication app = new EmployeeApplication();
		assertEquals(3, app.calculator(3, 0));
	}
}
