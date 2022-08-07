package com.example.demo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "Post")
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @OneToMany(orphanRemoval = true, mappedBy = "parent", fetch = FetchType.EAGER)
    private List<Post> embeddedPosts = new ArrayList<>();

    @ManyToOne
    private Post parent;

    @Column(name = "created_on")
    private LocalDate createdOn;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    private List<PostComment> comments = new ArrayList<>();

    public void addComment(PostComment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void addEmbeddedPost(Post embedded) {
        getEmbeddedPosts().add(embedded);
        embedded.setParent(this);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdOn=" + createdOn +
                ", comments=" + comments +
                ", parent=" + Optional.ofNullable(getParent()).map(Post::getTitle).orElse("[no parent]") +
                ", embedded=" + getEmbeddedPosts().stream().map(Post::getTitle).collect(Collectors.joining(",", "[", "]")) +
                '}';
    }

}
