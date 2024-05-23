package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Controller
public class Profile {

    private final UserService userService;

    @Autowired
    public Profile(UserService userService) {
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

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("username", currentUser(principal).getUsername());
        model.addAttribute("user", currentUser(principal));
        model.addAttribute("avatar", getProfilePictureAsBase64(currentUser(principal)));
        return "profile"; // Возвращает имя шаблона без расширения .html
    }

    @PostMapping("/uploadPhoto")
    public String uploadPhoto(@AuthenticationPrincipal OAuth2User principal, @RequestParam("croppedImage") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                // Обработка и сохранение bytes в базе данных
                userService.saveProfilePicture(currentUser(principal).getId(), bytes);
                System.out.println("Файл успешно загружен");
                return "redirect:/profile";
            } catch (Exception e) {
                System.out.println("Ошибка загрузки файла");
                return "redirect:/profile";

            }
        } else {
            System.out.println("Файл не загружен");
            return "redirect:/profile";

        }
    }

    @PostMapping("/updateUsername")
    public String updateUsername(@AuthenticationPrincipal OAuth2User principal, @RequestParam("username") String newUsername) {
        if (!newUsername.isEmpty()) {
            try {
                userService.saveUsername(currentUser(principal).getId(), newUsername);
                System.out.println("Имя пользователя успешно загружено");
                return "redirect:/profile";
            } catch (Exception e) {
                System.out.println("Ошибка загрузки имени пользователя");
                return "redirect:/profile";

            }
        } else {
            System.out.println("Имя пользователя не загружено");
            return "redirect:/profile";

        }
    }

}


