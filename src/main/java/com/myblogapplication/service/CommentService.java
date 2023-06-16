package com.myblogapplication.service;

import com.myblogapplication.repository.CommentRepository;
import com.myblogapplication.entity.Comment;
import com.myblogapplication.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    PostService postService;
    @Autowired
    CommentRepository commentRepository;

    public List<Comment> findCommentsByPostId(int id) {
        return commentRepository.findByPostId(id);
    }

    public void deleteByCommentId(int commentId) {
        commentRepository.deleteById(commentId);
    }

    public void saveComment(int postId, String name, String email, String newComment) {
        Post post = postService.getPost(postId);
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setEmail(email);
        comment.setName(name);
        LocalDate createdAt = LocalDate.now();
        comment.setCreatedAt(createdAt);
        comment.setPost(post);
        commentRepository.save(comment);
    }

    public void updateComment(int commentId, String newComment) {
        Comment existingComment = commentRepository.findById(commentId);
        LocalDate updatedAt = LocalDate.now();
        existingComment.setUpdatedAt(updatedAt);
        existingComment.setComment(newComment);
        commentRepository.save(existingComment);
    }

    public List<Comment> findAllComments() {
        return commentRepository.findAll();
    }

    public void addComment(Comment newComment, int postId) {
        Comment comment = new Comment();
        Post post = postService.getPost(postId);
        comment.setComment(newComment.getComment());
        comment.setName(newComment.getName());
        comment.setEmail(newComment.getEmail());
        comment.setCreatedAt(LocalDate.now());
        comment.setPost(post);
        commentRepository.save(comment);
    }

    public boolean commentUpdate(int commentId, Comment comment) {
        Comment oldComment = commentRepository.findById(commentId);
        if (oldComment.getComment().equals(comment.getComment())) {
            return false;
        }
        oldComment.setComment(comment.getComment());
        oldComment.setUpdatedAt(LocalDate.now());
        commentRepository.save(oldComment);
        return true;
    }

    public Comment findCommentById(int commentId) {
        return commentRepository.findById(commentId);
    }
}