package com.myblogapplication.restcontroller;

import com.myblogapplication.entity.Comment;
import com.myblogapplication.entity.Post;
import com.myblogapplication.service.CommentService;
import com.myblogapplication.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/comment")
public class CommentRestController {

    @Autowired
    CommentService commentService;
    @Autowired
    PostService postService;

    @GetMapping("/get/all")
    public List<Comment> getAllComments() {
        return commentService.findAllComments();
    }

    @PostMapping("/add/{postId}")
    public ResponseEntity<String> saveComment(@PathVariable int postId, @RequestBody Comment comment) {
        Post existingPost = postService.getPost(postId);
        if (existingPost == null) {
            return new ResponseEntity<>("this postId doesn't exist", HttpStatus.BAD_REQUEST);
        }
        if (comment.getComment() == null || comment.getComment().isEmpty()) {
            return new ResponseEntity<>("comment field is empty", HttpStatus.OK);
        } else if (comment.getName() == null || comment.getName().isEmpty()) {
            return new ResponseEntity<>("name field is empty", HttpStatus.OK);
        } else if (comment.getEmail() == null || comment.getEmail().isEmpty()) {
            return new ResponseEntity<>("email field is empty", HttpStatus.OK);
        } else {
            commentService.addComment(comment, postId);
        }
        return new ResponseEntity<>("comment saved for the post" + " " + postId, HttpStatus.OK);
    }

    @PutMapping("/update/{postId}/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isPostOwner(authentication,#postId)")
    public ResponseEntity<String> updateComment(@PathVariable int postId, @PathVariable int commentId, @RequestBody Comment comment) {
        Comment existingComment = commentService.findCommentById(commentId);
        List<Comment> comments = commentService.findCommentsByPostId(postId);
        if ((comments == null || comments.isEmpty()) && comment.getPost().getId() != postId) {
            return new ResponseEntity<>("this comment doesn't exist for this post", HttpStatus.NOT_FOUND);
        }
        if (existingComment == null) {
            return new ResponseEntity<>("this commentId doesn't exist ", HttpStatus.BAD_REQUEST);
        }
        Boolean isCommentUpdate = commentService.commentUpdate(commentId, comment);
        if (!isCommentUpdate) {
            return new ResponseEntity<>("You have not updated the comment", HttpStatus.OK);
        }
        return new ResponseEntity<>("Updated the comment Successfully", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{postId}/{commentId}")
    @PreAuthorize("hasAuthority('ADMIN') or @securityService.isPostOwner(authentication,#postId)")
    public ResponseEntity<String> deleteComment(@PathVariable int postId, @PathVariable int commentId) {
        List<Comment> comments = commentService.findCommentsByPostId(postId);
        Comment comment = commentService.findCommentById(commentId);
        if ((comments == null || comments.isEmpty()) && comment.getPost().getId() != postId) {
            return new ResponseEntity<>("this commentId doesn't exist for this post", HttpStatus.NOT_FOUND);
        }
        if (comment == null) {
            return new ResponseEntity<>("this commentId doesn't exist", HttpStatus.BAD_REQUEST);
        }
        commentService.deleteByCommentId(commentId);
        return new ResponseEntity<>("deleted the comment successfully", HttpStatus.OK);
    }
}