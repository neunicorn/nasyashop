package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp(){
        Product product1 = Product.builder()
                .name("Asics Novablast 5")
                .description("Sepatu asics yang nyaman untuk dipakai sebagai daily trainer")
                .price(new BigDecimal("2000000.00"))
                .stockQuantity(10)
                .weight(new BigDecimal("250"))
                .userId(1L)
                .build();

        Product product2 = Product.builder()
                .name("Asics Gel-Colonimbus")
                .description("Rasakan sensasi berjalan seperti diatas awan!")
                .price(new BigDecimal("2500000.00"))
                .stockQuantity(10)
                .weight(new BigDecimal("270"))
                .userId(1L)
                .build();

        Product product3 = Product.builder()
                .name("Asics GT")
                .description("Cara pertama mencoba sepatu asics")
                .price(new BigDecimal("1000000.00"))
                .stockQuantity(10)
                .weight(new BigDecimal("285"))
                .userId(1L)
                .build();

        Product product4 = Product.builder()
                .name("Asics Gel-Cayano Lite 32")
                .description("Sensasikan berjalan diatan jelly")
                .price(new BigDecimal("2690000.00"))
                .stockQuantity(10)
                .weight(new BigDecimal("265"))
                .userId(1L)
                .build();

        Product product5 = Product.builder()
                .name("Asics Unisex Lyte Classics")
                .description("Sensasikan berjalan diatan jelly")
                .price(new BigDecimal("1390000.00"))
                .stockQuantity(10)
                .weight(new BigDecimal("295"))
                .userId(1L)
                .build();

        List<Product> products = Arrays.asList(product1, product2, product3, product4, product5);
        productRepository.saveAll(products);
    }


    @Test
    void test_ProductRepo_findByNamePageable_ShouldReturnProducts() {

        Pageable pageable = PageRequest.of(0, 10);
        String search = "Asics";
        String name = "%" + search.toLowerCase() + "%";

        //ACT
        List<Product> products=  productRepository.findByNamePageable(name, pageable).getContent();

        //Assert
        assertNotNull(products);
        assertThat(products).size().isEqualTo(5);
    }

    @Test
    void findByCategory() {
    }

    @Test
    void findByPageable() {

        Pageable pageable = PageRequest.of(0, 10);

        //ACT
        List<Product> products=  productRepository.findByPageable(pageable).getContent();

        //Assert
        assertNotNull(products);
        assertThat(products).size().isEqualTo(5);
    }

    @Test
    void findByIdWithPessimisticLock() {
    }
}