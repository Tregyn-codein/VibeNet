package com.example.vibenet.repository;

import com.example.vibenet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // Здесь могут быть дополнительные методы запросов, если нужно
}