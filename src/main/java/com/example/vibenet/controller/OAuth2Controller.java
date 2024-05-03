package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.vibenet.service.UserService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;


class ImageDownloader {

    public static byte[] downloadImageAsBlob(String imageUrl) {
        try (InputStream in = new URL(imageUrl).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

@Controller
public class OAuth2Controller {

    private final UserService userService;

    public OAuth2Controller(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String username = principal.getAttribute("name");
        String picture_url = principal.getAttribute("picture");
        byte[] pictureData = ImageDownloader.downloadImageAsBlob(picture_url);
        User user = userService.findOrCreateUser(email, username, pictureData);
        // Перенаправление на страницу запроса никнейма, если необходимо
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Имя вашего шаблона страницы логина
    }
}