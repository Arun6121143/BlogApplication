package com.myblogapplication.repository;

import com.myblogapplication.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    @Query(value = "SELECT * FROM comments WHERE post_id = :id", nativeQuery = true)
    List<Comment> findByPostId(int id);

    Comment findById(int id);
}