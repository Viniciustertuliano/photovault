package com.PhotoVault.repository;

import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PhotographerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withDatabaseName("photovault_test");

    @Autowired
    private PhotographerRepository photographerRepository;

    private Photographer testPhotographer;

    @BeforeEach
    void setUp(){
        testPhotographer = new Photographer();
        testPhotographer.setName("Vinicius test");
        testPhotographer.setEmail("vinicius@test.com");
        testPhotographer.setPassword("123456");
        testPhotographer.setRole(UserRole.PHOTOGRAPHER);
        photographerRepository.save(testPhotographer);
    }


    @Test
    @DisplayName("Should save Photographer successfully")
    void shouldCreatePhotographer(){
        Photographer photographer = photographerRepository.findAll().getFirst();

        assertThat(photographer.getId()).isNotNull();
        assertThat(photographer.getName()).isEqualTo(testPhotographer.getName());
        assertThat(photographer.getEmail()).isEqualTo(testPhotographer.getEmail());
        assertThat(photographer.getRole()).isEqualTo(testPhotographer.getRole());
    }

    @Test
    @DisplayName("Should find Photographer by email successfully")
    void shouldFindPhotographerByEmail(){
        Optional<Photographer> result = photographerRepository.findByEmail(testPhotographer.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testPhotographer.getEmail());
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist(){
        Optional<Photographer> result = photographerRepository.findByEmail("test@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update Photographer successfully")
    void shouldUpdatePhotographer(){
        Photographer photographerResult = photographerRepository.findAll().getFirst();

        photographerResult.setName("vinicius");

        Photographer photographerAlt = photographerRepository.save(photographerResult);

        assertThat(photographerAlt.getId()).isEqualTo(photographerResult.getId());
        assertThat(photographerAlt.getEmail()).isEqualTo(photographerResult.getEmail());
        assertThat(photographerAlt.getName()).isEqualTo("vinicius");
        assertThat(photographerAlt.getName()).isNotEqualTo("Cecilia");

        Optional<Photographer> retDB = photographerRepository.findById(photographerResult.getId());
        assertThat(retDB).isPresent();
        assertThat(retDB.get().getName()).isEqualTo("vinicius");


    }

    @Test
    @DisplayName("Should delete Photographer successfully")
    void shouldDeletePhotographer(){
        photographerRepository.delete(testPhotographer);

        assertThat(photographerRepository.findById(testPhotographer.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return all Photographers")
    void shouldFindAllPhotographers(){
        Photographer testOtherPhotographer = new Photographer();
        testOtherPhotographer.setName("Cecilia test");
        testOtherPhotographer.setEmail("Cecilia@test.com");
        testOtherPhotographer.setPassword("123456");
        testOtherPhotographer.setRole(UserRole.PHOTOGRAPHER);

        Photographer testOtherPhotographer2 = new Photographer();
        testOtherPhotographer2.setName("Thayna test");
        testOtherPhotographer2.setEmail("Thayna@test.com");
        testOtherPhotographer2.setPassword("123456");
        testOtherPhotographer2.setRole(UserRole.PHOTOGRAPHER);


        photographerRepository.saveAll(List.of(testOtherPhotographer, testOtherPhotographer2));

        List<Photographer> result = photographerRepository.findAll();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(Photographer::getName)
                .containsExactlyInAnyOrder("Vinicius test", "Cecilia test", "Thayna test");

        assertThat(result)
                .extracting(Photographer::getEmail)
                .contains("vinicius@test.com", "Cecilia@test.com", "Thayna@test.com");
    }

    @Test
    @DisplayName("Should find Photographer by ID successfully")
    void shouldFindPhotographerById(){
        Optional<Photographer> result = photographerRepository.findById(testPhotographer.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testPhotographer.getId());
    }

    @Test
    @DisplayName("Should return empty when ID does not exist")
    void shouldReturnEmptyWhenIdDoesNotExist(){
        Optional<Photographer> result = photographerRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should not allow duplicate email")
    void shouldNotAllowDuplicateEmail(){
        Photographer testOtherPhotographer = new Photographer();
        testOtherPhotographer.setName("vinicius test");
        testOtherPhotographer.setEmail("vinicius@test.com");
        testOtherPhotographer.setPassword("123456");
        testOtherPhotographer.setRole(UserRole.PHOTOGRAPHER);

        assertThatThrownBy(() -> {
                photographerRepository.saveAndFlush(testOtherPhotographer);
            }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should not throw exception when deleting with non-existent ID")
    void shouldNotThrowExceptionWhenDeletingWithNonExistentId(){
        assertThatCode(() -> {
            photographerRepository.deleteById(999L);
        }).doesNotThrowAnyException();
    }
}
