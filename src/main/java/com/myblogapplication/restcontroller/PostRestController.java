package com.myblogapplication.restcontroller;

import com.myblogapplication.entity.Post;
import com.myblogapplication.entity.User;
import com.myblogapplication.service.PostService;
import com.myblogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/post")
public class PostRestController {
    @Autowired
    PostService postService;
    @Autowired
    UserService userService;

    @PostMapping("/add/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isOwner(authentication,#userId)")
    public ResponseEntity<String> savePost(@PathVariable int userId, @RequestBody Post post,
                                           @RequestParam(value = "author", required = false) String author) {
        User existingUser = userService.findUserById(userId);
        if (existingUser == null) {
            return new ResponseEntity<>("User Id doesn't exist", HttpStatus.OK);
        }
        if (!postService.addPost(userId, post, author)) {
            return new ResponseEntity<>("any one field is missing please add it", HttpStatus.OK);
        }
        return new ResponseEntity<>("post saved successfully for user with userId" + " " + userId, HttpStatus.OK);
    }

    @GetMapping("/get/all/posts")
    public List<Post> getAllPosts() {
        return postService.findAllPosts();
    }

    @GetMapping("/get/by/postId/{postId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isPostOwner(authentication,#postId)")
    public ResponseEntity<?> getPostById(@PathVariable int postId) {
        Post existingPost = postService.getPost(postId);
        if (existingPost == null) {
            return new ResponseEntity<>("postId doesn't exist", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(existingPost, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{postId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isPostOwner(authentication,#postId)")
    public ResponseEntity<String> deletePost(@PathVariable int postId) {
        Post existingPost = postService.getPost(postId);
        if (existingPost == null) {
            return new ResponseEntity<>("postId doesn't exist" + " " + postId, HttpStatus.OK);
        }
        postService.deleteByPostId(postId);
        return new ResponseEntity<>("Deleted Post with id" + postId, HttpStatus.OK);
    }

    @PutMapping("/update/{postId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isPostOwner(authentication,#postId)")
    public ResponseEntity<String> updatePost(@PathVariable int postId, @RequestBody Post updatedPost) {
        Post existingPost = postService.getPost(postId);
        if (existingPost == null) {
            return new ResponseEntity<>("postId doesn't exist" + postId, HttpStatus.OK);
        }
        postService.updatePost(postId, updatedPost);
        return new ResponseEntity<>("post updated successfully", HttpStatus.OK);
    }

    @GetMapping("/search/{search}")
    public ResponseEntity<?> searchPost(@PathVariable String search) {
        List<Post> postList = postService.searchPosts(search);
        if (postList == null || postList.isEmpty()) {
            String message = "No posts available for this search";
            return new ResponseEntity<>(message, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(postList, HttpStatus.OK);
        }
    }

    @GetMapping("/filters")
    public ResponseEntity<?> postFilter(@RequestParam(value = "author", required = false) Integer authorId,
                                        @RequestParam(value = "tagId", required = false) List<Integer> tagIds,
                                        @RequestParam(value = "Date", required = false) String date) {
        List<Post> filteredPosts = postService.findFilteredPosts(authorId, tagIds, date);
        if (filteredPosts == null || filteredPosts.isEmpty()) {
            return new ResponseEntity<>("no posts available", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredPosts, HttpStatus.OK);
    }

    @GetMapping("/pagination")
    public ResponseEntity<List<Post>> getPostsByPagination(@RequestParam(value = "start") int page,
                                                           @RequestParam(value = "limit") int limit) {
        Page<Post> postPage = postService.getAllPosts(page / 10 + 1, limit);
        List<Post> postList = postPage.getContent();
        if (postList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    @GetMapping("/sort")
    public ResponseEntity<List<Post>> getPostsBySorting(@RequestParam(value = "sortDirection") String sortOrder,
                                                        @RequestParam(value = "sortBy") String sortField) {
        List<Post> sortPostsList = postService.sortPosts(sortOrder, sortField);
        if (sortPostsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(sortPostsList, HttpStatus.OK);
    }
}