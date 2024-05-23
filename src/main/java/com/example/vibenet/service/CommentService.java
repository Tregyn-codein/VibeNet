package com.example.vibenet.service;

import com.example.vibenet.entity.Comment;
import com.example.vibenet.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> findAllComments() {
        return commentRepository.findAll();
    }

    public Optional<Comment> findCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public List<Comment> getCommentsByPostId(Long postId, int offset, int limit) {
        int page = offset / limit; // Рассчитываем номер страницы
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").ascending());
        Page<Comment> commentsPage = commentRepository.findByPostId(postId, pageable);
        return commentsPage.getContent();
    }

    public long countCommentsByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // Дополнительные методы, связанные с бизнес-логикой комментариев
}