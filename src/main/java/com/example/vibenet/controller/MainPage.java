package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.entity.Post;
import com.example.vibenet.service.ImageService;
import com.example.vibenet.service.UserService;
import com.example.vibenet.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


@Controller
public class MainPage {

    private final UserService userService;
    private final PostService postService;
    private final ImageService imageService;

    @Autowired
    public MainPage(UserService userService, PostService postService, ImageService imageService) {
        this.userService = userService;
        this.postService = postService;
        this.imageService = imageService;
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

    public String convertBlobToBase64String(byte[] blob) {
        return Base64.getEncoder().encodeToString(blob);
    }

    @GetMapping("/")
    public String mainpage(@AuthenticationPrincipal OAuth2User principal, Model model) {
        List<Post> posts = postService.findAllPostsByOrderByCreatedAtDesc();
        List<String> base64Images = new ArrayList<>();
        Map<Long, List<String>> postImagesMap = new HashMap<>();

        for (Post post : posts) {
            String base64Image = convertBlobToBase64String(post.getAuthor().getProfilePicture());
            base64Images.add(base64Image);
            List<String> imagesBase64 = imageService.getImagesByPost(post).stream()
                    .map(image -> convertBlobToBase64String(image.getImage()))
                    .collect(Collectors.toList());
            postImagesMap.put(post.getId(), imagesBase64);
        }


        // Получаем количество постов от автора
        long postsCountByAuthor = postService.countPostsByAuthor(currentUser(principal));
        // Получаем общее количество постов
        long totalPostsCount = postService.countAllPosts();
        // Получаем количество постов за сегодня
        long todayPostsCount = postService.countPostsFromToday();

        // Добавляем данные в модель
        model.addAttribute("postsCountByAuthor", postsCountByAuthor);
        model.addAttribute("totalPostsCount", totalPostsCount);
        model.addAttribute("todayPostsCount", todayPostsCount);
        model.addAttribute("posts", posts);
        model.addAttribute("base64Images", base64Images);
        model.addAttribute("postImagesMap", postImagesMap);
        model.addAttribute("user", currentUser(principal));
        model.addAttribute("username", currentUser(principal).getUsername());
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        return "index"; // Возвращает имя шаблона без расширения .html
    }

    @PostMapping("/create-post")
    public ResponseEntity<?> createPost(@RequestParam("content") String content,
                                        @RequestParam("onlyForFollowers") Boolean onlyForFollowers,
                                        @RequestPart("images") MultipartFile[] images,
                                        @AuthenticationPrincipal OAuth2User principal) {
        // Создание нового поста на основе данных из postData
        Post post = new Post();
        post.setContent(content);
        post.setAuthor(currentUser(principal));
        post.setCreatedAt(new Date());
        post.setOnlyForFollowers(onlyForFollowers);

        // Сохранение поста в базе данных
        Long postId = postService.save(post);
        Post savedPost = postService.findPostById(postId).orElse(null);

        for (MultipartFile imageFile : images) {
            if (!imageFile.isEmpty()) {
                imageService.saveImage(savedPost, imageFile);
            }
        }

        // Возвращаем ответ
        return ResponseEntity.ok(Collections.singletonMap("message", "Пост успешно создан"));
    }

    @DeleteMapping("/delete-post/{postId}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal OAuth2User principal) {

        // Находим пост по id
        Optional<Post> post = postService.findPostById(postId);
        if (post.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Проверяем, является ли текущий пользователь автором поста
        if (!post.get().getAuthor().getUsername().equals(currentUser(principal).getUsername())) {
            // Если нет, возвращаем статус запрещено
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете удалить этот пост");
        }

        // Удаляем пост
        postService.deletePost(postId);

        // Возвращаем ответ об успешном удалении
        return ResponseEntity.ok().body("Пост успешно удален");
    }

}


