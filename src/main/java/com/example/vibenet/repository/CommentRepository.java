package com.example.vibenet.repository;

import com.example.vibenet.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Здесь могут быть дополнительные методы запросов, если нужно
}