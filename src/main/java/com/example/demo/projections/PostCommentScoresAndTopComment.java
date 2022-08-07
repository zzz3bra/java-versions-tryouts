package com.example.demo.projections;

import org.springframework.beans.factory.annotation.Value;

public interface PostCommentScoresAndTopComment {

    String getTitle();

//    @Value("#{target.comments[0].score}")
    int getCommentsScore();

//    @Value("#{target.comments[0].review}")
    String getCommentsReview();
}
