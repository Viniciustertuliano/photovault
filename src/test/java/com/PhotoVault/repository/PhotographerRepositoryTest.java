package com.PhotoVault.repository;

import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@ActiveProfiles("test")
@DataJpaTest
public class PhotographerRepositoryTest {

    @Autowired
    private PhotographerRepository photographerRepository;

    private Photographer createPhotographer(String name, String email){
        Photographer photographer = new Photographer();
        photographer.setName(name);
        photographer.setEmail(email);
        photographer.setPassword("123456");
        photographer.setRole(UserRole.PHOTOGRAPHER);
        return photographer;
    }


    @Test
    @DisplayName("Should save Photographer successfully")
    void shouldCreatePhotographer(){

        Photographer photographer = createPhotographer("vinicius", "vinicius@test.com");
        Photographer photographerSalvo = photographerRepository.save(photographer);

        assertThat(photographerSalvo.getId()).isNotNull();
        assertThat(photographerSalvo.getName()).isEqualTo(photographer.getName());
        assertThat(photographerSalvo.getEmail()).isEqualTo(photographer.getEmail());
        assertThat(photographerSalvo.getRole()).isEqualTo(photographer.getRole());
    }

    @Test
    @DisplayName("Should find Photographer by email successfully")
    void shouldFindPhotographerByEmail(){
        Photographer photographer = createPhotographer("Cecilia", "cecilia@test.com");

        photographerRepository.save(photographer);

        Optional<Photographer> result = photographerRepository.findByEmail(photographer.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(photographer.getEmail());
    }

    @Test
    @DisplayName("Should return empty when email does not exist")
    void shouldReturnEmptyWhenEmailDoesNotExist(){
        Photographer photographer = createPhotographer("Cecilia", "cecilia@test.com");

        photographerRepository.save(photographer);

        Optional<Photographer> result = photographerRepository.findByEmail("test@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should update Photographer successfully")
    void shouldUpdatePhotographer(){


        Photographer photographer = createPhotographer("Cecilia", "cecilia@test.com");

        Photographer photographerSave = photographerRepository.save(photographer);

        photographerSave.setName("vinicius");

        Photographer photographerAlt = photographerRepository.save(photographerSave);

        assertThat(photographerAlt.getId()).isEqualTo(photographerSave.getId());
        assertThat(photographerAlt.getEmail()).isEqualTo(photographerSave.getEmail());
        assertThat(photographerAlt.getName()).isEqualTo("vinicius");
        assertThat(photographerAlt.getName()).isNotEqualTo("Cecilia");

        Optional<Photographer> retDB = photographerRepository.findById(photographerSave.getId());
        assertThat(retDB).isPresent();
        assertThat(retDB.get().getName()).isEqualTo("vinicius");


    }

    @Test
    @DisplayName("Should delete Photographer successfully")
    void shouldDeletePhotographer(){
        Photographer photographer = createPhotographer("Cecilia", "cecilia@test.com");

        Photographer photographerSave = photographerRepository.save(photographer);

        photographerRepository.delete(photographerSave);

        assertThat(photographerRepository.findById(photographerSave.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return all Photographers")
    void shouldFindAllPhotographers(){
        Photographer photographer1 = createPhotographer("Cecilia", "cecilia@test.com");
        Photographer photographer2 = createPhotographer("Vinicius", "vinicius@test.com");
        Photographer photographer3 = createPhotographer("Ana", "ana@test.com");

        photographerRepository.saveAll(List.of(photographer1, photographer2, photographer3));

        List<Photographer> result = photographerRepository.findAll();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(Photographer::getName)
                .containsExactlyInAnyOrder("Cecilia", "Vinicius", "Ana");

        assertThat(result)
                .extracting(Photographer::getEmail)
                .contains("cecilia@test.com", "vinicius@test.com", "ana@test.com");
    }

    @Test
    @DisplayName("Should find Photographer by ID successfully")
    void shouldFindPhotographerById(){
        Photographer photographer = createPhotographer("Cecilia", "cecilia@test.com");
        Photographer photographerSave = photographerRepository.save(photographer);
        Optional<Photographer> result = photographerRepository.findById(photographerSave.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(photographerSave.getId());
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
        Photographer photographer1 = createPhotographer("Cecilia", "cecilia@test.com");
        Photographer photographer2 = createPhotographer("Vinicius", "cecilia@test.com");

        photographerRepository.save(photographer1);

        assertThatThrownBy(() -> {
            photographerRepository.save(photographer2);
            photographerRepository.flush();
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
