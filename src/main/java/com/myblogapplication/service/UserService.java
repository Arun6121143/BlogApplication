package com.myblogapplication.service;

import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.User;
import com.myblogapplication.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    @Lazy
    PostService postService;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Boolean saveUserRegistrationDetails(User user, String confirmPassword) {
        String password = user.getPassword();
        String email = user.getEmail();
        boolean isEmailExist = isEmailExists(email);
        if (isEmailExist) {
            if (password.equals(confirmPassword)) {
                User newUser = new User();
                BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                newUser.setName(user.getName());
                newUser.setPassword(passwordEncoder.encode(user.getPassword()));
                newUser.setEmail(user.getEmail());
                newUser.setRole("AUTHOR");
                userRepository.save(newUser);
                return true;
            }
        }
        return false;
    }

    public boolean isEmailExists(String email) {
        User user = userRepository.findByEmail(email);
        return user == null;
    }

    public Boolean saveUser(String email, String password, String name) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User existingUser = userRepository.findByEmail(email);
        if(existingUser!=null){
            return false;
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setName(name);
        newUser.setRole("AUTHOR");
        userRepository.save(newUser);
        return true;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserById(int id) {
        return userRepository.findById(id);
    }

    public void deleteByUserId(int userId) {
        userRepository.deleteById(userId);
    }

    public boolean updateUser(int userId, String user) {
        JSONObject jsonObject = new JSONObject(user);
        User oldUser = userRepository.findById(userId);
        List<Post> posts = postService.findPostByUserId(userId);
        for(Post post : posts){
            post.setAuthor(jsonObject.getString("name"));
            postService.saveAuthor(post);
        }
        if (oldUser == null) {
            return false;
        }
        oldUser.setEmail(jsonObject.getString("email"));
        oldUser.setName(jsonObject.getString("name"));
        userRepository.save(oldUser);
        return true;
    }
}