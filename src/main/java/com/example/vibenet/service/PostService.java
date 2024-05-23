package com.example.vibenet.service;

import com.example.vibenet.entity.Post;
import com.example.vibenet.entity.User;
import com.example.vibenet.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final ImageService imageService;

    @Autowired
    public PostService(PostRepository postRepository, ImageService imageService) {
        this.postRepository = postRepository;
        this.imageService = imageService;
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public List<Post> findLatestPosts(int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable).getContent();
    }

    public List<Post> findAllPostsByOrderByCreatedAtDesc() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Post> findPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id).get();
        imageService.deleteImagesByPost(post);
        postRepository.deleteById(id);
    }

    public Long save(Post post) {
        return postRepository.save(post).getId();
    }

    // Поиск постов по автору
    public List<Post> findPostsByAuthor(User author) {
        return postRepository.findByAuthor(author);
    }

    // Поиск постов за сегодня
    public List<Post> findPostsFromToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return postRepository.findPostsFromToday(startOfDay, endOfDay);
    }

    public long countPostsByAuthor(User author) {
        return postRepository.countByAuthor(author);
    }

    public long countAllPosts() {
        return postRepository.count();
    }

    public long countPostsFromToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return postRepository.countPostsFromToday(startOfDay, endOfDay);
    }

    public Page<Post> findPaginatedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findAll(pageable);
    }

    public Page<Map<String, Object>> findPaginatedPostsWithImages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findAll(pageable);

        return postsPage.map(post -> {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("content", post.getContent());
            postMap.put("createdAt", post.getCreatedAt());
            postMap.put("author", post.getAuthor());
            postMap.put("onlyForFollowers", post.getOnlyForFollowers());
            List<String> imagesBase64 = imageService.getImagesByPost(post).stream()
                    .map(image -> Base64.getEncoder().encodeToString(image.getImage()))
                    .collect(Collectors.toList());
            postMap.put("images", imagesBase64);

            return postMap;
        });
    }

    public Page<Map<String, Object>> findPaginatedPostsBySubscriptions(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.findByAuthorFollowersId(userId, pageable);

        return postsPage.map(post -> {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("content", post.getContent());
            postMap.put("createdAt", post.getCreatedAt());
            postMap.put("author", post.getAuthor());
            postMap.put("onlyForFollowers", post.getOnlyForFollowers());
            List<String> imagesBase64 = imageService.getImagesByPost(post).stream()
                    .map(image -> Base64.getEncoder().encodeToString(image.getImage()))
                    .collect(Collectors.toList());
            postMap.put("images", imagesBase64);

            return postMap;
        });
    }


}
