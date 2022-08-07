package com.example.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Random;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity(name = "PostComment")
@Table(name = "post_comment")
public class PostComment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Post post;

    private String review;

    private int score = new Random().nextInt(100);

    private LocalDateTime creationDate = LocalDateTime.now();

    public PostComment(String review) {
        this.review = review;
    }

}
