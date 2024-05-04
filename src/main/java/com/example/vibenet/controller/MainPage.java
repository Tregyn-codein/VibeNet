package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.entity.Post;
import com.example.vibenet.service.UserService;
import com.example.vibenet.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;


@Controller
public class MainPage {

    private final UserService userService;
    private final PostService postService;

    @Autowired
    public MainPage(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    public User currentUser(@AuthenticationPrincipal OAuth2User principal) {
        User user = userService.findUserByEmail(principal.getAttribute("email")).orElseThrow(() -> new RuntimeException("User not found"));;
        return user;
    }

    public ResponseEntity<byte[]> getProfilePicture(User user) {
        byte[] pictureData = user.getProfilePicture();
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(pictureData);
    }

    public String getProfilePictureAsBase64(User user) {
        if (user.getProfilePicture() != null) {
            byte[] pictureData = user.getProfilePicture();
            return Base64.getEncoder().encodeToString(pictureData);
        }
        return null; // Или возвращайте данные изображения по умолчанию
    }

    @GetMapping("/")
    public String mainpage(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("user", currentUser(principal));
        model.addAttribute("username", currentUser(principal).getUsername());
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        model.addAttribute("posts", postService.findAllPostsByOrderByCreatedAtDesc());
        return "index"; // Возвращает имя шаблона без расширения .html
    }

    @PostMapping("/create-post")
    public ResponseEntity<?> createPost(@RequestBody Map<String, Object> postData,
                                        @AuthenticationPrincipal OAuth2User principal) {
        // Создание нового поста на основе данных из postData
        Post post = new Post();
        post.setContent((String) postData.get("content"));
        post.setAuthor(currentUser(principal));
        post.setCreatedAt(new Date());
        post.setOnlyForFollowers((Boolean) postData.get("onlyForFollowers"));

        // Сохранение поста в базе данных
        postService.save(post);

        // Возвращаем ответ
        return ResponseEntity.ok(Collections.singletonMap("message", "Пост успешно создан"));
    }

}


