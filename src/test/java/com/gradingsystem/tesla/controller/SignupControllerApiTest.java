package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.BaseIntegrationTest;
import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SignupControllerApiTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll(); // Reset DB before each test
    }

    @Tag("api")
    @Test
    void shouldSignupSuccessfully() throws Exception {
        // Given: A valid signup request
        String requestBody = "username=teststudent&email=test@example.com&password=StrongPass123";

        // When: Sending a POST request to /signup
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody))

                // Then: Expect HTTP 302 Redirect to /dashboard
                .andExpect(status().isFound())  // 302 Found (Redirect)
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Tag("api")
    @Test
    void shouldFailSignupWhenUsernameAlreadyExists() throws Exception {
        // Given: A user already exists in the database
        studentRepository.save(new Student(null, "teststudent", "test@example.com", "password"));

        // When: Sending a POST request with the same username
        String requestBody = "username=teststudent&email=newemail@example.com&password=NewPass123";
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody))

                // Then: Expect HTTP 302 Redirect to /signup?error=...
                .andExpect(status().isFound())  // 302 Found (Redirect)
                .andExpect(redirectedUrlPattern("/signup?error=*"));
    }

    @Tag("api")
    @Test
    void shouldFailSignupWhenEmailAlreadyExists() throws Exception {
        // Given: A user already exists with the same email
        studentRepository.save(new Student(null, "uniqueuser", "test@example.com", "password"));

        // When: Sending a POST request with the same email
        String requestBody = "username=newstudent&email=test@example.com&password=NewPass123";
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody))

                // Then: Expect HTTP 302 Redirect to /signup?error=...
                .andExpect(status().isFound())  // 302 Found (Redirect)
                .andExpect(redirectedUrlPattern("/signup?error=*"));
    }

    @Tag("api")
    @Test
    void shouldFailSignupWithInvalidEmailFormat() throws Exception {
        // Given: An invalid email format
        String requestBody = "username=teststudent&email=invalid-email&password=StrongPass123";

        // When: Sending a POST request
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody))

                // Then: Expect HTTP 302 Redirect to /signup?error=...
                .andExpect(status().isFound())  // 302 Found (Redirect)
                .andExpect(redirectedUrlPattern("/signup?error=*"));
    }
}