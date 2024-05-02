package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.entity.User.*;
import com.example.vibenet.repository.UserRepository;
import com.example.vibenet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;


@Controller
public class MainPage {

    private final UserService userService;

    @Autowired
    public MainPage(UserService userService) {
        this.userService = userService;
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
        // Можно добавить атрибуты модели, которые будут использоваться в шаблоне
        System.out.println(currentUser(principal).getProfilePicture());
        model.addAttribute("username", currentUser(principal).getUsername());
//        model.addAttribute("avatar", getProfilePicture(currentUser(principal)));
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        return "index"; // Возвращает имя шаблона без расширения .html
    }

}


