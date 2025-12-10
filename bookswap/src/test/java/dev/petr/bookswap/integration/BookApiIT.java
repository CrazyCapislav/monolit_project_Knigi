package dev.petr.bookswap.integration;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.petr.bookswap.entity.Role;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.repository.UserRepository;

@AutoConfigureMockMvc
@SuppressWarnings("null")
class BookApiIT extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;

    Long uid;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        User user = userRepo.save(User.builder()
                .email("m@l").passwordHash("h").displayName("Mock User")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        uid = user.getId();
    }

    @Test
    void create_and_page() throws Exception {
        mvc.perform(post("/api/v1/books")
                        .header("X-User-Id", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title":"Clean Code",
                              "author":"R. Martin",
                              "condition":"GOOD",
                              "genre_ids":[]
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"));

        mvc.perform(get("/api/v1/books?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }
}
