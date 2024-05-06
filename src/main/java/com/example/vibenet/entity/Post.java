package com.example.vibenet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Getter
    @Setter
    @Column(nullable = false, length = 2000, columnDefinition = "TEXT")
    private String content;

    @Getter
    @Setter
    @Column(nullable = false)
    private Boolean onlyForFollowers;

    @Getter
    @Setter
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt = new Date();

    // ... другие поля, геттеры и сеттеры
}