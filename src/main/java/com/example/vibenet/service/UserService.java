package com.example.vibenet.service;

import com.example.vibenet.entity.User;
import com.example.vibenet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User findOrCreateUser(String email, String username, byte[] pictureData) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setProfilePicture(pictureData);
                    return userRepository.save(newUser);
                });
    }

    public void followUser(Long currentUserId, Long userIdToFollow) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(/* ... */);
        User userToFollow = userRepository.findById(userIdToFollow).orElseThrow(/* ... */);
        currentUser.getFollowing().add(userToFollow);
        userRepository.save(currentUser);
    }

    public void unfollowUser(Long currentUserId, Long userIdToUnfollow) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(/* ... */);
        User userToUnfollow = userRepository.findById(userIdToUnfollow).orElseThrow(/* ... */);
        currentUser.getFollowing().remove(userToUnfollow);
        userRepository.save(currentUser);
    }

    public void saveProfilePicture(Long userId, byte[] pictureData) {
        User user = userRepository.findById(userId).orElseThrow(/* ... */);
        user.setProfilePicture(pictureData);
        userRepository.save(user);
    }

    public byte[] getProfilePicture(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfilePicture)
                .orElse(null);
    }


}
