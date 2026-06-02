package com.PhotoVault.repository;

import com.PhotoVault.BaseIntegrationTest;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FolderRepositoryTest  {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14-alpine"))
            .withDatabaseName("photovault_test");

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private FolderRepository folderRepository;

    private Photographer testPhotographer;

    private Folder testFolder;

    @BeforeEach
    void setUp() {
        folderRepository.deleteAll();
        photographerRepository.deleteAll();

        testPhotographer = new Photographer();
        testPhotographer.setName("vinicius");
        testPhotographer.setEmail("vinicius@test.com");
        testPhotographer.setPassword("123456");
        testPhotographer.setRole(UserRole.PHOTOGRAPHER);
        photographerRepository.save(testPhotographer);

        testFolder = new Folder();
        testFolder.setName("testFolder");
        testFolder.addOwner(testPhotographer);
        testFolder.setCreatedAt(java.time.LocalDateTime.now());
        folderRepository.save(testFolder);
    }

    @Test
    @DisplayName("Should save Folder successfully")
    void shouldCreateFolder(){

        assertThat(testFolder.getId()).isNotNull();
        assertThat(testFolder.getName()).isEqualTo("testFolder");
        assertThat(testFolder.getOwners().stream().findFirst().get().getId()).isEqualTo(testPhotographer.getId());
    }

    @Test
    @DisplayName("Should find Folder by name successfully")
    void shouldFindFolderByName(){
        Optional<Folder> foundFolder = folderRepository.findByName("testFolder");

        System.out.println(testFolder.getName());
        System.out.println(testFolder.getId());


        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getId()).isEqualTo(testFolder.getId());
    }

    @Test
    @DisplayName("Should return empty when name does not exist")
    void shouldReturnEmptyWhenNameDoesNotExist(){
        Optional<Folder> foundFolder = folderRepository.findByName("testFolderNotExist");
        assertThat(foundFolder).isEmpty();
    }

    @Test
    @DisplayName("Should update Folder successfully")
    void shouldUpdateFolder(){
        testFolder.setName("updatedFolderName");
        Folder updatedFolder = folderRepository.save(testFolder);

        assertThat(updatedFolder.getName()).isEqualTo("updatedFolderName");
        assertThat(updatedFolder.getName()).isNotEqualTo("testFolder");
        assertThat(testFolder.getId()).isEqualTo(updatedFolder.getId());

        Optional<Folder> foundFolder = folderRepository.findById(testFolder.getId());
        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getName()).isEqualTo("updatedFolderName");
    }

    @Test
    @DisplayName("Should delete Folder successfully")
    void shouldDeleteFolder(){
        folderRepository.delete(testFolder);

        assertThat(folderRepository.findById(testFolder.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return all Folders successfully")
    void shouldFindAllFolder(){
        Folder testFolderOther = new Folder();
        testFolderOther.setName("testFolderOther");
        testFolderOther.addOwner(testPhotographer);

        Folder testFolderOther2 = new Folder();
        testFolderOther2.setName("testFolderOther2");
        testFolderOther2.addOwner(testPhotographer);

        folderRepository.saveAll(List.of(testFolder, testFolderOther, testFolderOther2));

        List<Folder> foldersList = folderRepository.findAll();

        assertThat(foldersList).isNotEmpty();
        assertThat(foldersList).hasSize(3);
        assertThat(foldersList).
                extracting(Folder::getName).
                containsExactlyInAnyOrder("testFolder", "testFolderOther", "testFolderOther2");

    }

    @Test
    @DisplayName("Should find Folder by ID successfully")
    void shouldFindFolderById(){
        Optional<Folder> foundFolder = folderRepository.findById(testFolder.getId());

        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getId()).isEqualTo(testFolder.getId());
    }

    @Test
    @DisplayName("Should find Folders by Owner ID successfully")
    void ShouldFindFoldersByOwnerId(){
        Folder testFolderOther = new Folder();
        testFolderOther.setName("testFolderOther");
        testFolderOther.addOwner(testPhotographer);

        Folder testFolderOther2 = new Folder();
        testFolderOther2.setName("testFolderOther2");
        testFolderOther2.addOwner(testPhotographer);


        folderRepository.saveAll(List.of(testFolder, testFolderOther, testFolderOther2));

        Page<Folder> foundFolders = folderRepository.findByOwnerId(testPhotographer.getId(), PageRequest.of(0, 10));

        assertThat(foundFolders).isNotEmpty();
        assertThat(foundFolders).hasSize(3);
        assertThat(foundFolders).
                extracting(Folder::getName).
                containsExactlyInAnyOrder("testFolder", "testFolderOther", "testFolderOther2");
    }

    @Test
    @DisplayName("Should return empty when ID does not exist")
    void shouldReturnEmptyWhenIdDoesNotExist(){
        Optional<Folder> foundFolder = folderRepository.findById(999L);
        assertThat(foundFolder).isEmpty();
    }

    @Test
    @DisplayName("Should not throw exception when deleting with non-existent ID")
    void shouldNotThrowExceptionWhenDeletingWithNonExistentId(){
        assertThatCode(() -> {
            folderRepository.deleteById(999L);
        }).doesNotThrowAnyException();
    }
}
