package com.myblogapplication.controller;

import com.myblogapplication.entity.Comment;
import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.User;
import com.myblogapplication.service.CommentService;
import com.myblogapplication.service.PostService;
import com.myblogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    PostService postService;
    @Autowired
    UserService userService;

    @PostMapping("/save-comment")
    public String saveComment(@RequestParam("postId") int postId,
                              @RequestParam("name") String name,
                              @RequestParam("email") String email,
                              @RequestParam("comment") String newComment,
                              Model model,
                              Principal principal) {
        commentService.saveComment(postId, name, email, newComment);
        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        List<Comment> comments = commentService.findCommentsByPostId(postId);
        Post post = postService.getPost(postId);
        model.addAttribute("post", post);
        model.addAttribute("commentList", comments);
        return "post";
    }

    @GetMapping("/comment/delete")
    @PreAuthorize("hasAuthority('ADMIN') or @postService.getPost(#postId).getAuthor() == " +
            "@userService.findUserByEmail(#principal.getName()).getName()")
    public String deleteComment(@RequestParam("postId") int postId, @RequestParam("commentId") int commentId,
                                Principal principal) {
        commentService.deleteByCommentId(commentId);
        return "redirect:/post" + postId;
    }

    @PostMapping("/comment/update")
    @PreAuthorize("hasAuthority('ADMIN') or @postService.getPost(#postId).getAuthor() == " +
            "@userService.findUserByEmail(#principal.getName()).getName()")
    public String updateComment(@RequestParam("postId") int postId,
                                @RequestParam("commentId") int commentId,
                                @RequestParam("newComment") String newComment,
                                Principal principal) {
        commentService.updateComment(commentId, newComment);
        return "redirect:/post" + postId;
    }
}