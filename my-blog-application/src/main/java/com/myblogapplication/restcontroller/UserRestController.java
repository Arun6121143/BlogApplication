package com.myblogapplication.restcontroller;

import com.myblogapplication.entity.User;
import com.myblogapplication.service.PostService;
import com.myblogapplication.service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/user")
public class UserRestController {
    @Autowired
    UserService userService;
    @Autowired
    PostService postService;

    @PostMapping("/save")
    public ResponseEntity<String> saveUser(@RequestBody String user) {
        JSONObject jsonObject = new JSONObject(user);
        String email = jsonObject.getString("email");
        String password = jsonObject.getString("password");
        String name = jsonObject.getString("name");
        String confirmPassword = jsonObject.getString("confirmPassword");
        if (email == null || email.isEmpty()) {
            return new ResponseEntity<>("email not found", HttpStatus.NOT_FOUND);
        } else if (password == null || password.isEmpty()) {
            return new ResponseEntity<>("password not found", HttpStatus.NOT_FOUND);
        } else if (confirmPassword == null || confirmPassword.isEmpty()) {
            return new ResponseEntity<>("confirm password not found", HttpStatus.NOT_FOUND);
        } else if (name == null || name.isEmpty()) {
            return new ResponseEntity<>("name not found", HttpStatus.NOT_FOUND);
        } else if (!password.equals(confirmPassword)) {
            return new ResponseEntity<>("password do not match", HttpStatus.NOT_FOUND);
        } else {
           boolean isUserExist =  userService.saveUser(email, password, name);
           if(!isUserExist){
               return new ResponseEntity<>("user with this email already exists", HttpStatus.OK);
           }
        }
        return new ResponseEntity<>("user saved successfully", HttpStatus.OK);
    }

    @GetMapping("/get/all")
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/get/by/id/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isOwner(authentication, #userId)")
    public ResponseEntity<?> getUserById(@PathVariable int userId) {
        User existingUser = userService.findUserById(userId);
        if (existingUser == null) {
            return new ResponseEntity<>("User Id doesn't exist", HttpStatus.OK);
        }
        User user = userService.findUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isOwner(authentication,#userId)")
    public ResponseEntity<String> deleteUser(@PathVariable int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            return new ResponseEntity<>("User Id doesn't exist", HttpStatus.OK);
        }
        userService.deleteByUserId(userId);
        return new ResponseEntity<>("Deleted User with id" + " " + userId, HttpStatus.OK);
    }

    @PutMapping("/update/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isOwner(authentication,#userId)")
    public ResponseEntity<String> updateUser(@PathVariable int userId, @RequestBody String user) {
        User existingUser = userService.findUserById(userId);
        if (existingUser == null) {
            return new ResponseEntity<>("User Id doesn't exist", HttpStatus.OK);
        }
        JSONObject jsonObject = new JSONObject(user);
        String email = jsonObject.getString("email");
        String name = jsonObject.getString("name");
        if (email == null || email.isEmpty()) {
            return new ResponseEntity<>("email not found", HttpStatus.NOT_FOUND);
        } else if (name == null || name.isEmpty()) {
            return new ResponseEntity<>("name not found", HttpStatus.NOT_FOUND);
        } else {
            Boolean userIdPresent = userService.updateUser(userId, user);
            if (userIdPresent == false) {
                return new ResponseEntity<>("user with this userId" + " " + userId + " " + "not present", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("user updated successfully", HttpStatus.OK);
    }
}