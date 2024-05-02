package com.example.vibenet.repository;

import com.example.vibenet.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    // Здесь могут быть дополнительные методы запросов, если нужно
}