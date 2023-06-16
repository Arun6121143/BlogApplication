package com.myblogapplication.repository;

import com.myblogapplication.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Post findById(int id);

    List<Post> findByUserId(int id);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.title LIKE %:keyword% OR p.author LIKE %:keyword% " +
            "OR t.name LIKE %:keyword%" + "OR p.content LIKE %:keyword%")
    List<Post> searchPosts(String keyword);
}