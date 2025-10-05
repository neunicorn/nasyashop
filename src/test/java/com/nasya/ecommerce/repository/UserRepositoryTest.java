package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp(){

        User user = User.builder()
                .username("baehq12")
                .email("zbaehaki@gmail.com")
                .password("encodedPassword123!")
                .enabled(true)
                .build();
        userRepository.save(user);

    }

    @Test
    void test_UserRepository_FindByKeyword_ReturnsUser() {
        Optional<User> user = userRepository.findByKeyword("baehq12");

        assertTrue(user.isPresent());
        assertEquals("baehq12", user.get().getUsername());
        assertEquals("zbaehaki@gmail.com", user.get().getEmail());
    }

    @Test
    void test_UserRepository_ExistsByUsername_returnsTrue() {
        assertTrue(userRepository.existsByUsername("baehq12"));
    }

    @Test
    void test_UserRepository_ExistsByEmail_ReturnsTrue() {
        assertTrue(userRepository.existsByEmail("zbaehaki@gmail.com"));
    }
}