package com.example.vibenet.repository;

import com.example.vibenet.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Здесь могут быть дополнительные методы запросов, если нужно
}