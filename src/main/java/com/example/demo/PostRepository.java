package com.example.demo;

import com.example.demo.projections.PostCommentScoresAndTopComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByIdInAndIdIsNotNull(List<Long> ids);

    PostCommentScoresAndTopComment findTopByOrderByCommentsScoreDesc();

}
