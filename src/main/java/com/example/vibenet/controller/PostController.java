package com.example.vibenet.controller;

import com.example.vibenet.entity.Comment;
import com.example.vibenet.entity.Post;
import com.example.vibenet.entity.User;
import com.example.vibenet.service.CommentService;
import com.example.vibenet.service.ImageService;
import com.example.vibenet.service.PostService;
import com.example.vibenet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final ImageService imageService;
    private final CommentService commentService;

    @Autowired
    public PostController(UserService userService, PostService postService, ImageService imageService, CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.imageService = imageService;
        this.commentService = commentService;
    }

    public User currentUser(@AuthenticationPrincipal OAuth2User principal) {
        User user = userService.findUserByEmail(principal.getAttribute("email")).orElseThrow(() -> new RuntimeException("User not found"));;
        return user;
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long postId,
                                                     @RequestParam(defaultValue = "0") int offset,
                                                     @RequestParam(defaultValue = "3") int limit) {
        List<Comment> comments = commentService.getCommentsByPostId(postId, offset, limit);
        long totalComments = commentService.countCommentsByPostId(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("totalComments", totalComments);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId, @RequestBody Map<String, String> commentData, @AuthenticationPrincipal OAuth2User principal) {
        String content = commentData.get("content");
        // Здесь должна быть логика для определения автора комментария, например, из контекста безопасности
        User author = currentUser(principal);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        Post post = postService.findPostById(postId).get();
        comment.setPost(post);

        Comment savedComment = commentService.saveComment(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }

    public String getProfilePictureAsBase64(User user) {
        if (user.getProfilePicture() != null) {
            byte[] pictureData = user.getProfilePicture();
            return Base64.getEncoder().encodeToString(pictureData);
        }
        return null; // Или возвращайте данные изображения по умолчанию
    }

    @GetMapping("/search")
    public String searchPostsPage(@RequestParam String query, Model model, @AuthenticationPrincipal OAuth2User principal) {
        model.addAttribute("query", query);
        // Получаем количество найденных постов
        long postsCount = postService.countSearchPosts(query);

        // Добавляем данные в модель
        model.addAttribute("postsCount", postsCount);
        model.addAttribute("user", currentUser(principal));
        model.addAttribute("username", currentUser(principal).getUsername());
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        return "search"; // Имя шаблона для отображения результатов поиска
    }

    @GetMapping("/search/results")
    public ResponseEntity<Page<Map<String, Object>>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> searchResults = postService.searchPosts(query, page, size);
        return ResponseEntity.ok(searchResults);
    }
}