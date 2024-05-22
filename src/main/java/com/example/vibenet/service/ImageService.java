package com.example.vibenet.service;

import com.example.vibenet.entity.Image;
import com.example.vibenet.entity.Post;
import com.example.vibenet.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Transactional
    public Image saveImage(Post post, MultipartFile file) {
        Image image = new Image();
        image.setPost(post);
        try {
            image.setImage(file.getBytes());
        } catch (IOException e) {
            // Обработка исключения, например, можно залогировать ошибку
            // и/или пробросить своё пользовательское исключение
            throw new RuntimeException("Ошибка при сохранении изображения", e);
        }
        return imageRepository.save(image);
    }

//    public Image save(Image image) {return imageRepository.save(image);}

    public List<Image> getImagesByPost(Post post) {
        return imageRepository.findByPost(post);
    }

    public Map<Long, List<String>> getImagesByPostIds(List<Long> postIds) {
        Map<Long, List<String>> postImagesMap = new HashMap<>();
        for (Long postId : postIds) {
            List<Image> images = imageRepository.findByPostId(postId);
            List<String> imagesBase64 = images.stream()
                    .map(image -> Base64.getEncoder().encodeToString(image.getImage()))
                    .collect(Collectors.toList());
            postImagesMap.put(postId, imagesBase64);
        }
        return postImagesMap;
    }

    public void deleteImagesByPost(Post post) {
        List<Image> images = getImagesByPost(post);
        imageRepository.deleteAll(images);
    }
    // Другие методы для работы с изображениями...
}