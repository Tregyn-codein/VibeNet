package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.entity.Post;
import com.example.vibenet.service.ImageService;
import com.example.vibenet.service.UserService;
import com.example.vibenet.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
//        int size = 2; // Количество постов на странице
//
//        List<Post> latestPosts = postService.findLatestPosts(size);
//
////         Собираем ID постов для получения изображений
//        List<Long> postIds = latestPosts.stream().map(Post::getId).collect(Collectors.toList());
//        Map<Long, List<String>> postImagesMap = imageService.getImagesByPostIds(postIds);
//
//        List<String> base64Images = new ArrayList<>();
//
//        for (Post post : latestPosts) {
//            String base64Image = convertBlobToBase64String(post.getAuthor().getProfilePicture());
//            base64Images.add(base64Image);
//        }
//
//        model.addAttribute("posts", latestPosts);
//        model.addAttribute("postImagesMap", postImagesMap);
//        model.addAttribute("base64Images", base64Images);

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
        model.addAttribute("user", currentUser(principal));
        model.addAttribute("username", currentUser(principal).getUsername());
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        return "index"; // Возвращает имя шаблона без расширения .html
    }

    @GetMapping("/loadMorePosts")
    public ResponseEntity<Page<Map<String, Object>>> loadMorePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> postsWithImages = postService.findPaginatedPostsWithImages(page, size);
        return ResponseEntity.ok(postsWithImages);
    }

    @GetMapping("/{userId}/subscriptions/posts")
    public ResponseEntity<Page<Map<String, Object>>> loadPostsBySubscriptions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal OAuth2User principal) {
        // Проверка, что текущий пользователь запрашивает свои собственные подписки
        if (!currentUser(principal).getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<Map<String, Object>> posts = postService.findPaginatedPostsBySubscriptions(userId, page, size);
        return ResponseEntity.ok(posts);
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


