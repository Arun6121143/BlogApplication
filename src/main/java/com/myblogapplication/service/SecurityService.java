package com.myblogapplication.service;

import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.User;
import com.myblogapplication.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    @Autowired
    private UserService userService;
    @Autowired
    private PostRepository postRepository;

    public boolean isOwner(Authentication authentication, int userId) {
        String email = authentication.getName();
        User user = userService.findUserById(userId);
        return user != null && user.getEmail().equals(email);
    }

    public boolean isPostOwner(Authentication authentication, int postId) {
        String email = authentication.getName();
        Post post = postRepository.findById(postId);
        User user = userService.findUserById(post.getUser().getId());
        return user != null && user.getEmail().equals(email);
    }
}