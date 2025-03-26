package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.BaseIntegrationTest;
import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unused")
@AutoConfigureMockMvc
class LoginControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll(); // Reset database before each test
    }

    // @Tag("api")
    @Test
    void shouldShowLoginPageWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login")); // Expect login page
    }

    // @Tag("api")
    @Test
    void shouldRedirectToDashboardWhenAlreadyLoggedIn() throws Exception {
        // Given: A logged-in student in session
        Student student = studentRepository.save(new Student(null, "testuser", "test@example.com", "password"));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInStudent", student);

        // When: Accessing login page
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isFound())  // 302 Redirect
                .andExpect(redirectedUrl("/dashboard"));
    }

    // @Tag("api")
    @Test
    void shouldRedirectToDashboardWhenAdminIsLoggedIn() throws Exception {
        // Given: An admin session
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("isAdmin", true);

        // When: Accessing login page
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isFound())  // 302 Redirect
                .andExpect(redirectedUrl("/dashboard"));
    }

    // @Tag("api")
    @Test
    void shouldLoginStudentSuccessfully() throws Exception {
        // Given: A student exists in the database
        Student student = studentRepository.save(new Student(null, "testuser", "test@example.com", "password"));

        // When: Logging in with correct credentials
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "password")
                .session(session))

                // Then: Expect redirect to dashboard
                .andExpect(status().isFound())  
                .andExpect(redirectedUrl("/dashboard"));

        // Verify session attributes
        assertThat(session.getAttribute("loggedInStudent")).isNotNull();
        assertThat(((Student) session.getAttribute("loggedInStudent")).getUsername()).isEqualTo("testuser");
        assertThat(session.getAttribute("id")).isEqualTo(student.getId());
    }

    // @Tag("api")
    @Test
    void shouldLoginAdminSuccessfully() throws Exception {
        // When: Logging in as admin
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "admin")  // Defined in application.properties
                .param("password", "admin") // Ensure this matches your @Value properties
                .session(session))

                // Then: Expect redirect to dashboard
                .andExpect(status().isFound())  
                .andExpect(redirectedUrl("/dashboard"));

        // Verify session attributes
        assertThat(session.getAttribute("isAdmin")).isEqualTo(true);
    }

    // @Tag("api")
    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        // Given: A student exists
        studentRepository.save(new Student(null, "testuser", "test@example.com", "password"));

        // When: Logging in with incorrect password
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "testuser")
                .param("password", "wrongpassword"))

                // Then: Stay on login page with error message
                .andExpect(status().isOk())  
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password"));
    }

    // @Tag("api")
    @Test
    void shouldFailLoginForNonExistentUser() throws Exception {
        // When: Logging in with a user that doesn't exist
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "unknown")
                .param("password", "password"))

                // Then: Stay on login page with error message
                .andExpect(status().isOk())  
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", "Invalid username or password"));
    }
}
