package com.example.vibenet.repository;

import com.example.vibenet.entity.Image;
import com.example.vibenet.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByPost(Post post);
}