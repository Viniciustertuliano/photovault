package com.PhotoVault.repository;

import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class FolderRepositoryTest {

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private FolderRepository folderRepository;

    private Photographer createPhotographer(){
        Photographer photographer = new Photographer();
        photographer.setName("vinicius");
        photographer.setEmail("vinicius@test.com");
        photographer.setPassword("123456");
        photographer.setRole(UserRole.PHOTOGRAPHER);
        return photographer;
    }

    private Folder createFolder(String name, Photographer owner){
        Folder folder = new Folder();
        folder.setName(name);
        folder.setOwner(owner);
        folder.setCreatedAt(java.time.LocalDateTime.now());
        return folder;
    }

    @Test
    @DisplayName("Should save Folder successfully")
    void shouldCreateFolder(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        Folder savedFolder = folderRepository.save(createFolder("testFolder", savedPhotographer));

        assertThat(savedFolder.getId()).isNotNull();
        assertThat(savedFolder.getName()).isEqualTo("testFolder");
        assertThat(savedFolder.getOwner().getId()).isEqualTo(savedPhotographer.getId());
    }

    @Test
    @DisplayName("Should find Folder by name successfully")
    void shouldFindFolderByName(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        Folder savedFolder = folderRepository.save(createFolder("testFolder", savedPhotographer));

        Optional<Folder> foundFolder = folderRepository.findByName("testFolder");

        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getId()).isEqualTo(savedFolder.getId());
    }

    @Test
    @DisplayName("Should return empty when name does not exist")
    void shouldReturnEmptyWhenNameDoesNotExist(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        folderRepository.save(createFolder("testFolder", savedPhotographer));

        Optional<Folder> foundFolder = folderRepository.findByName("testFolderNotExist");
        assertThat(foundFolder).isEmpty();
    }

    @Test
    @DisplayName("Should update Folder successfully")
    void shouldUpdateFolder(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        Folder savedFolder = folderRepository.save(createFolder("testFolder", savedPhotographer));

        savedFolder.setName("updatedFolderName");
        Folder updatedFolder = folderRepository.save(savedFolder);

        assertThat(updatedFolder.getName()).isEqualTo("updatedFolderName");
        assertThat(updatedFolder.getName()).isNotEqualTo("testFolder");
        assertThat(savedFolder.getId()).isEqualTo(updatedFolder.getId());

        Optional<Folder> foundFolder = folderRepository.findById(savedFolder.getId());
        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getName()).isEqualTo("updatedFolderName");

    }

    @Test
    @DisplayName("Should delete Folder successfully")
    void shouldDeleteFolder(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        Folder savedFolder = folderRepository.save(createFolder("testFolder", savedPhotographer));

        folderRepository.delete(savedFolder);

        assertThat(folderRepository.findById(savedFolder.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should return all Folders successfully")
    void shouldFindAllFolder(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());

        folderRepository.saveAll(List.of(
                                        createFolder("testFolder", savedPhotographer),
                                        createFolder("testFolder2", savedPhotographer),
                                        createFolder("testFolder3", savedPhotographer)));

        List<Folder> foldersList = folderRepository.findAll();

        assertThat(foldersList).isNotEmpty();
        assertThat(foldersList).hasSize(3);
        assertThat(foldersList).
                extracting(Folder::getName).
                containsExactlyInAnyOrder("testFolder", "testFolder2", "testFolder3");

    }

    @Test
    @DisplayName("Should find Folder by ID successfully")
    void shouldFindFolderById(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());
        Folder savedFolder = folderRepository.save(createFolder("testFolder", savedPhotographer));

        Optional<Folder> foundFolder = folderRepository.findById(savedFolder.getId());

        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getId()).isEqualTo(savedFolder.getId());
    }

    @Test
    @DisplayName("Should find Folders by Owner ID successfully")
    void ShouldFindFoldersByOwnerId(){
        Photographer savedPhotographer = photographerRepository.save(createPhotographer());

        folderRepository.saveAll(List.of(
                createFolder("testFolder", savedPhotographer),
                createFolder("testFolder2", savedPhotographer),
                createFolder("testFolder3", savedPhotographer)));

        List<Folder> foundFolders = folderRepository.findByOwnerId(savedPhotographer.getId());

        assertThat(foundFolders).isNotEmpty();
        assertThat(foundFolders).hasSize(3);
        assertThat(foundFolders).
                extracting(Folder::getName).
                containsExactlyInAnyOrder("testFolder", "testFolder2", "testFolder3");
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
