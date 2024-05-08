package com.example.vibenet.service;

import com.example.vibenet.entity.Image;
import com.example.vibenet.entity.Post;
import com.example.vibenet.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    public void deleteImagesByPost(Post post) {
        List<Image> images = getImagesByPost(post);
        imageRepository.deleteAll(images);
    }
    // Другие методы для работы с изображениями...
}