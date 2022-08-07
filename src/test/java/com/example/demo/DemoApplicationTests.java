package com.example.demo;

import com.example.demo.records.Response;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*"))
@Slf4j
class DemoApplicationTests {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void setUp() throws Exception {
        Post post1 = new Post();

        post1.setTitle("High-Performance Java Persistence eBook has been released!");
        post1.setCreatedOn(LocalDate.of(2016, 8, 30));

        entityManager.persist(post1);

        post1.addComment(new PostComment("Excellent!"));
        post1.addComment(new PostComment("Great!"));

        Post post2 = new Post();

        post2.setTitle("High-Performance Java Persistence paperback has been released!");
        post2.setCreatedOn(LocalDate.of(2016, 10, 12));

        entityManager.persist(post2);

        Post post3 = new Post();

        post3.setTitle("High-Performance Java Persistence Mach 1 video course has been released!");
        post3.setCreatedOn(LocalDate.of(2018, 1, 30));

        entityManager.persist(post3);

        Post post4 = new Post();

        post4.setTitle("High-Performance Java Persistence Mach 2 video course has been released!");
        post4.setCreatedOn(LocalDate.of(2018, 5, 8));

        entityManager.persist(post4);

        entityManager.flush();
        log.info("setUp done");
    }

    //https://www.loicmathieu.fr/wordpress/en/informatique/java-19-quoi-de-neuf/

    @Test
    public void virtualThreads() throws Exception {
        Post post5 = new Post();
        post5.setTitle("Meta-post");
        post5.setCreatedOn(LocalDate.of(2022, 8, 30));
        postRepository.findAll().forEach(post5::addEmbeddedPost);

        entityManager.persist(post5);
        entityManager.flush();
        Long post5Id = post5.getId();
        entityManager.clear();

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Future<List<Post>> usingFindMethodFuture = executorService.submit(() -> {
            log.info("usingFindMethod");//fetches entity and relationships:comments and parent posts using n+1=11 selects
            List<Post> usingFindMethod = postRepository.findAllById(List.of(post5Id));
            entityManager.clear();
            return usingFindMethod;
        });

        Future<List<Post>> usingFindCustomMethodFuture = executorService.submit(() -> {
            log.info("usingFindCustomMethod");//fetches entity and relationships:comments and parent posts using n+1=11 selects
            List<Post> usingFindCustomMethod = postRepository.findAllByIdInAndIdIsNotNull(List.of(post5Id));
            entityManager.clear();
            return usingFindCustomMethod;
        });

        Future<List<Post>> future = executorService.submit(() -> {
            log.info("usingJpqlQuery");//fetches entity and relationships:comments and parent posts using n+1=11 selects
            List<Post> usingJpqlQuery = entityManager.createQuery("""
                            select p
                            from Post p
                            where p.id IN :ids
                            """, Post.class)
                    .setParameter("ids", List.of(post5Id))
                    .getResultList();
            entityManager.clear();
            return usingJpqlQuery;
        });

        List<Post> posts = usingFindMethodFuture.get();
        log.info("{}", posts);

        List<Post> posts1 = usingFindCustomMethodFuture.get();
        log.info("{}", posts1);

        List<Post> posts2 = future.get();
        log.info("{}", posts2);

        Response response = handle();
        log.info("{}", response);
    }

    Response handle() throws ExecutionException, InterruptedException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Future<String> postTitle1 = scope.fork(() -> {
                String title = postRepository.findAll().get(0).getTitle();
                log.info("executing Future<String> postTitle1");
                return title;
            });
            Future<String> postTitle2 = scope.fork(() -> {
                String title = postRepository.findAll().get(10).getTitle();
                log.info("executing Future<String> postTitle2");
                return title;
            });
            Future<Integer> maxScore = scope.fork(() -> {
                int commentsScore = postRepository.findTopByOrderByCommentsScoreDesc().getCommentsScore();
                log.info("executing Future<Integer> commentsScore");
                return commentsScore;
            });

            scope.join();          // Join both forks
            scope.throwIfFailed(); // ... and propagate errors

            // Here, both forks have succeeded, so compose their results
            return new Response(postTitle1.resultNow(), postTitle2.resultNow(), maxScore.resultNow());
        }
    }
}
