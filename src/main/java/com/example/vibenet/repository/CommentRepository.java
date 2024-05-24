package com.example.vibenet.repository;

import com.example.vibenet.entity.Comment;
import com.example.vibenet.entity.Image;
import com.example.vibenet.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    List<Comment> findByPost(Post post);
    long countByPostId(Long postId);
}