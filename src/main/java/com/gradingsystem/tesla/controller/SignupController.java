package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.model.Student;
import com.gradingsystem.tesla.service.StudentService;
import jakarta.servlet.http.HttpSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/signup")
    public String showSignupPage(HttpSession session, Model model) {
        Student loggedInStudent = (Student) session.getAttribute("loggedInStudent");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (isAdmin != null && isAdmin) {
            // If session exists, redirect to the dashboard
            return "redirect:/dashboard";
        }

        if (loggedInStudent != null) {
            // If session exists, redirect to the dashboard
            return "redirect:/dashboard";
        }
        model.addAttribute("student", new Student());
        return "signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@ModelAttribute("student") Student student, HttpSession session) {

        if (student.getUsername() == null || student.getPassword() == null) {
            return "redirect:/signup?error=Invalid input";
        }
        //  Verify Email Format
        if (!validateEmail(student.getEmail())){
            return "redirect:/signup?error=Invalid email";
        }
        try {
            Student added = studentService.saveStudent(student);

            if (added != null) {
                // Store the student in the session
                session.setAttribute("loggedInStudent", student);
                session.setAttribute("id", student.getId());
                return "redirect:/dashboard";
            } else {
                // If student is not added, redirect with an error
                return "redirect:/signup?error=Failed to create account";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/signup?error=Email already exists!";
        }
    }

    private boolean validateEmail(String email){
        String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
        
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}
