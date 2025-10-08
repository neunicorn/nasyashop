package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.config.ApiSecurityConfiguration;
import com.nasya.ecommerce.config.middleware.JwtAuthenticationFilter;
import com.nasya.ecommerce.entity.User;
import com.nasya.ecommerce.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest(
        excludeAutoConfiguration = {
                RedisAutoConfiguration.class,
                SecurityAutoConfiguration.class
        },
        excludeFilters = {
                // Exclude the custom configuration classes that demand web beans (HandlerExceptionResolver)
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        ApiSecurityConfiguration.class,
                        JwtAuthenticationFilter.class
                })}
)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Mocks are still necessary as a safety net
    @MockBean
    private CacheService cacheService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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