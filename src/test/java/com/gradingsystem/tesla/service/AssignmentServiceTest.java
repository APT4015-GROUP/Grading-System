// package com.gradingsystem.tesla.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import com.gradingsystem.tesla.model.Assignment;
import com.gradingsystem.tesla.repository.AssignmentRepository;
import com.gradingsystem.tesla.service.AssignmentService;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentServiceTest.class);

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Assignment sampleAssignment;

    @BeforeEach
    void setUp() {
        // Creating a sample assignment object for testing
        sampleAssignment = new Assignment();
        sampleAssignment.setId(1L);
        sampleAssignment.setTitle("Test Assignment");
        sampleAssignment.setDescription("This is a test assignment.");
    }

    @Tag("unit")
    @Test
    void testCreateAssignment_Success() {
        // Mock the repository behavior to return the saved assignment
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(sampleAssignment);

        // Call the service method
        Assignment createdAssignment = assignmentService.createAssignment(sampleAssignment);

        // Verify the result
        assertNotNull(createdAssignment);
        assertEquals(sampleAssignment.getId(), createdAssignment.getId());
        assertEquals(sampleAssignment.getTitle(), createdAssignment.getTitle());

        // Ensure the repository's save method was called
        verify(assignmentRepository, times(1)).save(sampleAssignment);

        logger.info("✅ testCreateAssignment_Success PASSED: Assignment created successfully with ID: {}",
                createdAssignment.getId());
    }

    @Tag("unit")
    @Test
    void testGetAssignmentById_Found() {
        // Mock repository to return the assignment when queried by ID
        when(assignmentRepository.findAssignmentById(1L)).thenReturn(sampleAssignment);

        // Call the service method
        Assignment foundAssignment = assignmentService.getAssignmentById(1L);

        // Verify the result
        assertNotNull(foundAssignment);
        assertEquals(sampleAssignment.getId(), foundAssignment.getId());
        assertEquals(sampleAssignment.getTitle(), foundAssignment.getTitle());

        // Ensure the repository's findById method was called
        verify(assignmentRepository, times(1)).findAssignmentById(1L);

        logger.info("✅ testGetAssignmentById_Found PASSED: Found assignment with ID: {}", foundAssignment.getId());
    }

    @Tag("unit")
    @Test
    void testGetAssignmentById_NotFound() {
        // Mock repository to return empty when queried by an invalid ID
        when(assignmentRepository.findAssignmentById(999L)).thenReturn(null);

        // Ensure an exception is thrown when assignment is not found
        RuntimeException exception = assertThrows(
                RuntimeException.class, // Replace with the actual exception thrown
                () -> assignmentService.getAssignmentById(999L));

        // check the exception message
        assertEquals("Assignment not found with ID: 999", exception.getMessage());

        // Verify that findById was called once
        verify(assignmentRepository, times(1)).findAssignmentById(999L);

        logger.info(
                "✅ testGetAssignmentById_NotFound PASSED: System correctly threw an exception for non-existent assignment ID 999.");
    }

    @Tag("unit")
    @Test
    void testGetAllAssignments() {
        // Mock repository to return a list of assignments
        List<Assignment> assignments = Arrays.asList(sampleAssignment, new Assignment());
        when(assignmentRepository.findAll()).thenReturn(assignments);

        // Call the service method
        List<Assignment> result = assignmentService.getAllAssignments();

        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());

        // Ensure the repository's findAll method was called
        verify(assignmentRepository, times(1)).findAll();

        logger.info("✅ testGetAllAssignments PASSED: Retrieved {} assignments successfully.", result.size());
    }

    @Tag("unit")
    @Test
    void testDeleteAssignment_Success() {
        // Mock repository to return an assignment when queried by ID
        when(assignmentRepository.existsById(1L)).thenReturn(true);

        // Call the service method
        assignmentService.deleteAssignment(1L);

        // Ensure the repository's delete method was called
        verify(assignmentRepository, times(1)).deleteById(sampleAssignment.getId());

        logger.info("✅ testDeleteAssignment_Success PASSED: Assignment with ID {} was deleted.",
                sampleAssignment.getId());
    }

    @Tag("unit")
    @Test
    void testDeleteAssignment_NotFound() {
        // Mock repository to return empty when queried by an invalid ID
        when(assignmentRepository.existsById(999L)).thenReturn(false);

        // Ensure an exception is thrown when trying to delete a non-existing assignment
        RuntimeException exception = assertThrows(
                RuntimeException.class, // Use RuntimeException if no custom exception is defined
                () -> assignmentService.deleteAssignment(999L));

        assertEquals("Assignment not found with ID: 999", exception.getMessage());

        // Ensure the repository's delete method was never called
        verify(assignmentRepository, never()).deleteById(anyLong());

        logger.info(
                "✅ testDeleteAssignment_NotFound PASSED: System correctly threw an exception when trying to delete non-existent assignment ID 999.");
    }
}