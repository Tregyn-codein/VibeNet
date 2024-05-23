package com.example.vibenet.repository;

import com.example.vibenet.entity.Post;
import com.example.vibenet.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthor(User author);
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    List<Post> findPostsFromToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    long countByAuthor(User author);
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    long countPostsFromToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    Page<Post> findByAuthorFollowersId(Long userId, Pageable pageable);
}