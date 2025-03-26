//package com.gradingsystem.tesla.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.gradingsystem.tesla.DTO.EvaluationDetails;
import com.gradingsystem.tesla.DTO.SubmissionDTO;
import com.gradingsystem.tesla.model.*;
import com.gradingsystem.tesla.repository.DocumentSubmissionRepository;
import com.gradingsystem.tesla.service.RetrieveEvaluationService;
import com.gradingsystem.tesla.service.RetrieveSubmissions;

import java.util.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class RetrieveServiceTests {

    @Mock
    private DocumentSubmissionRepository submissionRepository;

    @InjectMocks
    private RetrieveEvaluationService retrieveEvaluationService;

    @InjectMocks
    private RetrieveSubmissions retrieveSubmissions;

    private DocumentSubmission mockSubmission;

    @BeforeEach
    void setUp() {
        // Create a mock DocumentSubmission instance
        mockSubmission = new DocumentSubmission();
        mockSubmission.setGrade(85);
        mockSubmission.setSimilarityScore(30);
        mockSubmission.setEvaluationResults(Map.of("clarity", "good", "depth", "excellent"));
        
        Student student = new Student();
        student.setId(1L);
        student.setUsername("John Doe");
        student.setEmail("johndoe@example.com");
        mockSubmission.setStudent(student);
        
        Assignment assignment = new Assignment();
        assignment.setId(100L);
        mockSubmission.setAssignment(assignment);
    }

    @Tag("unit")
    @Test
    void testGetEvaluationDetails() {
        // Mock repository response
        when(submissionRepository.findByAssignmentIdAndStudentId(100L, 1L)).thenReturn(mockSubmission);
        
        // Call service method
        EvaluationDetails evaluationDetails = retrieveEvaluationService.getEvaluationDetails(100L, 1L);
        
        // Verify results
        assertNotNull(evaluationDetails);
        assertEquals(85, evaluationDetails.getGrade());
        assertEquals(30, evaluationDetails.getPlagiarismScore());
        assertEquals(2, evaluationDetails.getResults().size());
    }

    @Tag("unit")
    @Test
    void testGetSubmissions() {
        // Mock repository response
        when(submissionRepository.findByAssignmentId(100L)).thenReturn(List.of(mockSubmission));
        
        // Call service method
        List<SubmissionDTO> submissions = retrieveSubmissions.getSubmissions(100L);
        
        // Verify results
        assertNotNull(submissions);
        assertEquals(1, submissions.size());
        assertEquals("John Doe", submissions.get(0).getStudentName());
        assertEquals("johndoe@example.com", submissions.get(0).getStudentEmail());
        assertEquals(100L, submissions.get(0).getAssignmentId());
        assertEquals(1L, submissions.get(0).getStudentId());
    }

    @Tag("unit")
    @Test
    void testGetEvaluationDetails_SubmissionNotFound() {
        // Mock repository returning null
        when(submissionRepository.findByAssignmentIdAndStudentId(200L, 2L)).thenReturn(null);
        
        // Call service method and assert exception
        assertThrows(NullPointerException.class, () -> retrieveEvaluationService.getEvaluationDetails(200L, 2L));
    }

    @Tag("unit")
    @Test
    void testGetSubmissions_NoSubmissions() {
        // Mock repository returning empty list
        when(submissionRepository.findByAssignmentId(300L)).thenReturn(Collections.emptyList());
        
        // Call service method
        List<SubmissionDTO> submissions = retrieveSubmissions.getSubmissions(300L);
        
        // Verify that the result is empty
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }
}