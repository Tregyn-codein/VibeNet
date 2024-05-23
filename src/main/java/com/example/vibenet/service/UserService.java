package com.example.vibenet.service;

import com.example.vibenet.entity.User;
import com.example.vibenet.exception.ResourceNotFoundException;
import com.example.vibenet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public User followUser(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followerId));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followedId));

        follower.getFollowing().add(followed);
        return userRepository.save(follower);
    }

    public User unfollowUser(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followerId));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followedId));

        follower.getFollowing().remove(followed);
        return userRepository.save(follower);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followerId));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + followedId));

        return follower.getFollowing().contains(followed);
    }

    public Set<User> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return user.getFollowers();
    }

    public Set<User> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        return user.getFollowing();
    }

    public void saveProfilePicture(Long userId, byte[] pictureData) {
        User user = userRepository.findById(userId).orElseThrow(/* ... */);
        user.setProfilePicture(pictureData);
        userRepository.save(user);
    }

    public void saveUsername(Long userId, String username) {
        User user = userRepository.findById(userId).orElseThrow(/* ... */);
        user.setUsername(username);
        userRepository.save(user);
    }

    public byte[] getProfilePicture(Long userId) {
        return userRepository.findById(userId)
                .map(User::getProfilePicture)
                .orElse(null);
    }


}
