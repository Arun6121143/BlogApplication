package com.myblogapplication.controller;

import com.myblogapplication.entity.Comment;
import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.User;
import com.myblogapplication.service.CommentService;
import com.myblogapplication.service.PostService;
import com.myblogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PostController {
    @Autowired
    PostService postService;
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;

    @GetMapping("/new-post")
    public String showNewPost() {
        return "create-post";
    }

    @GetMapping("/")
    public String paginateAndSort(@RequestParam(value = "start", defaultValue = "1") int start,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit,
                                  Principal principal,
                                  Model model) {
        return paginateAndSort(start, limit, "publishedAt", "asc", principal, model);
    }

    @GetMapping("/page/{pageNumber}")
    public String paginateAndSort(@PathVariable(value = "pageNumber") int pageNumber,
                                  @RequestParam(value = "limit", defaultValue = "10") int limit,
                                  @RequestParam(value = "sortField", required = false, defaultValue = "publishedAt")
                                  String sortField,
                                  @RequestParam(value = "sortOrder", required = false, defaultValue = "publishedAt")
                                  String sortDirection,
                                  Principal principal,
                                  Model model) {

        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        Page<Post> page = postService.paginateAndSort(((pageNumber / 10) + 1), limit, sortField, sortDirection);
        List<Post> posts = page.getContent();
        model.addAttribute("start", pageNumber);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("postList", posts);
        model.addAttribute("allPosts", postService.findAllPosts());
        return "all-posts";
    }

    @GetMapping("/post{id}")
    public String showPost(@PathVariable int id, Model model, Principal principal) {
        Post post = postService.getPost(id);
        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        List<Comment> comments = commentService.findCommentsByPostId(id);
        model.addAttribute("post", post);
        model.addAttribute("commentList", comments);
        return "post";
    }

    @PostMapping("/save-post")
    public String savePost(@RequestParam("title") String title,
                           @RequestParam("tags") String tags,
                           @RequestParam("content") String content,
                           @RequestParam(value = "author", required = false) String author,
                           Principal principal) {
        postService.savePost(title, content, tags, author, principal);
        return "redirect:/";
    }

    @GetMapping("/posts/search")
    public String searchPosts(@RequestParam("search") String searchbar, Model model, Principal principal) {
        List<Post> posts = postService.searchPosts(searchbar);
        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        model.addAttribute("postList", posts);
        model.addAttribute("allPosts", postService.findAllPosts());
        model.addAttribute("start", 1);
        return "all-posts";
    }

    @GetMapping("/post/update/{postId}")
    @PreAuthorize("hasAuthority('ADMIN') or @postService.getPost(#postId).getAuthor() == " +
            "@userService.findUserByEmail(#principal.getName()).getName()")
    public String showUpdateForm(@PathVariable int postId, Model model, Principal principal) {
        Post existingPost = postService.getPost(postId);
        model.addAttribute("post", existingPost);
        return "update-post";
    }

    @PostMapping("/post/update")
    @PreAuthorize("hasAuthority('ADMIN') or @postService.getPost(#postId).getAuthor() == " +
            "@userService.findUserByEmail(#principal.getName()).getName()")
    public String updatePost(@RequestParam("postId") int postId,
                             @RequestParam("title") String title,
                             @RequestParam("tags") String tags,
                             @RequestParam("content") String content,
                             Principal principal,
                             Model model) {
        Post existingPost = postService.getPost(postId);
        postService.saveUpdatedPost(existingPost, tags, title, content, existingPost.getTags());
        List<Post> updatePostList = postService.findAllPosts();
        model.addAttribute("postList", updatePostList);
        return "redirect:/";
    }

    @GetMapping("/post/delete/{postId}")
    @PreAuthorize("hasAuthority('ADMIN') or @postService.getPost(#postId).getAuthor() == " +
            "@userService.findUserByEmail(#principal.getName()).getName()")
    public String deletePost(@PathVariable int postId, Principal principal) {
        postService.deleteByPostId(postId);
        return "redirect:/";
    }

    @GetMapping("/posts/filter")
    public String filterPosts(@RequestParam(value = "author", required = false) Integer authorId,
                              @RequestParam(value = "tagId", required = false) List<Integer> tagIds,
                              @RequestParam(value = "date", required = false) String date,
                              Principal principal,
                              Model model) {
        if (tagIds == null) {
            tagIds = new ArrayList<>();
        }
        List<Post> filteredPosts = postService.findFilteredPosts(authorId, tagIds, date);
        if (principal != null) {
            User user = userService.findUserByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        model.addAttribute("postList", filteredPosts);
        model.addAttribute("allPosts", postService.findAllPosts());
        model.addAttribute("start", 1);
        return "all-posts";
    }
}