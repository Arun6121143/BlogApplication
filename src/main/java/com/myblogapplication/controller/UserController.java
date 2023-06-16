package com.myblogapplication.controller;

import com.myblogapplication.entity.User;
import com.myblogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    @Autowired
    UserService userservice;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/save-registration")
    public String createUser(@ModelAttribute User user,
                             @RequestParam("confirm password") String confirmPassword,
                             Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordMismatch", true);
            return "register";
        }
        Boolean isSaveUser = userservice.saveUserRegistrationDetails(user, confirmPassword);
        if (!isSaveUser) {
            model.addAttribute("emailAlreadyExists", true);
            return "register";
        }
        return "redirect:/login";
    }
}