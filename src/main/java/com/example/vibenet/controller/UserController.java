package com.example.vibenet.controller;

import com.example.vibenet.entity.User;
import com.example.vibenet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public OAuth2User user(@AuthenticationPrincipal OAuth2User principal) {
        return principal;
    }

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile/{id}/profile-picture")
    public String uploadProfilePicture(@PathVariable Long id, @RequestParam("picture") MultipartFile pictureFile) {
        try {
            byte[] pictureData = pictureFile.getBytes();
            userService.saveProfilePicture(id, pictureData);
            return "redirect:/users/profile";
        } catch (IOException e) {
            // Обработка исключения
            return "error";
        }
    }

    @GetMapping("/users/{id}/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id) {
        byte[] pictureData = userService.getProfilePicture(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(pictureData);
    }

    @PostMapping("/{followerId}/follow/{followedId}")
    public ResponseEntity<User> followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        User updatedUser = userService.followUser(followerId, followedId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{followerId}/unfollow/{followedId}")
    public ResponseEntity<User> unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        User updatedUser = userService.unfollowUser(followerId, followedId);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{followerId}/isFollowing/{followedId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        boolean isFollowing = userService.isFollowing(followerId, followedId);
        return ResponseEntity.ok(isFollowing);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<Set<User>> getFollowers(@PathVariable Long userId) {
        Set<User> followers = userService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<Set<User>> getFollowing(@PathVariable Long userId) {
        Set<User> following = userService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }
}