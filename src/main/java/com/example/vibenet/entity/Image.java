package com.example.vibenet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Getter
    @Setter
    @Lob
    @Column(length = 655555)
    private byte[] image;

}