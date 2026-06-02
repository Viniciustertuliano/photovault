package com.PhotoVault;

import com.PhotoVault.entities.File;
import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import com.PhotoVault.services.StorageCleanupScheduler;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageCleanupIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private StorageCleanupScheduler storageCleanupScheduler;

    private Photographer testPhotographer;
    private Folder testFolder;
    private File testFile;

    private Long expiredFileId;
    private final String BUCKET_NAME = "photovault-test-bucket";
    private final String STORED_FILE_NAME = "image.jpg";

    @BeforeEach
    void setUp() {
        fileRepository.deleteAll();
        folderRepository.deleteAll();
        photographerRepository.deleteAll();

        testPhotographer = new Photographer();
        testPhotographer.setName("cleaning Teste");
        testPhotographer.setEmail("cleaning@teste.com");
        testPhotographer.setPassword("password");
        testPhotographer.setRole(UserRole.PHOTOGRAPHER);
        testPhotographer.setStorageQuotaBytes(5000000L);
        testPhotographer.setStorageUsedBytes(1024L);
        photographerRepository.save(testPhotographer);

        testFolder = new Folder();
        testFolder.setName("trash");
        testFolder.addOwner(testPhotographer);
        folderRepository.save(testFolder);

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(STORED_FILE_NAME)
                .build(), RequestBody.fromString("false data from the old image"));

        testFile = new File();
        testFile.setName("old_image.jpg");
        testFile.setStoredName(STORED_FILE_NAME);
        testFile.setPath(STORED_FILE_NAME);
        testFile.setSize(1024L);
        testFile.setContentType("image/jpeg");
        testFile.setFolder(testFolder);
        testFile = fileRepository.save(testFile);
        expiredFileId = testFile.getId();

        jdbcTemplate.update("UPDATE tb_files SET deleted_at = CURRENT_TIMESTAMP - INTERVAL '35 days' WHERE id = ?", expiredFileId);
    }

    @Test
    void shouldHardDeleteExpiredFilesFromDatabaseAndS3() {

        ListObjectsV2Response s3Before = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build());
        boolean existsBefore = s3Before.contents().stream().anyMatch(obj -> obj.key().equals(STORED_FILE_NAME));
        assertThat(existsBefore).isTrue();

        storageCleanupScheduler.cleanupExpiredStorage();

        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tb_files WHERE id = ?",
                Integer.class,
                expiredFileId
        );
        assertThat(dbCount).isEqualTo(0);

        ListObjectsV2Response s3After = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build());
        boolean existsAfter = s3After.contents().stream().anyMatch(obj -> obj.key().equals(STORED_FILE_NAME));
        assertThat(existsAfter).isFalse();

        Photographer updatedPhoto = photographerRepository.findById(testPhotographer.getId()).orElseThrow();
        assertThat(updatedPhoto.getStorageUsedBytes()).isEqualTo(0L);
    }

    @Test
    void shouldNotDeleteFilesWithinThe30DaysGracePeriod() {
        // 1. Criar um NOVO ficheiro no S3 e no Banco
        String recentFileName = "recent_image.jpg";
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(recentFileName)
                .build(), RequestBody.fromString("Recent data that cannot disappear."));

        File recentFile = new File();
        recentFile.setName("recent.jpg");
        recentFile.setStoredName(recentFileName);
        recentFile.setPath(recentFileName);
        recentFile.setSize(500L);
        recentFile.setContentType("image/jpeg");
        recentFile.setFolder(testFolder);
        recentFile = fileRepository.save(recentFile);
        Long recentFileId = recentFile.getId();

        jdbcTemplate.update("UPDATE tb_files SET deleted_at = CURRENT_TIMESTAMP - INTERVAL '10 days' WHERE id = ?", recentFileId);

        storageCleanupScheduler.cleanupExpiredStorage();

        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tb_files WHERE id = ?",
                Integer.class,
                recentFileId
        );
        assertThat(dbCount).isEqualTo(1);

        ListObjectsV2Response s3After = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .build());
        boolean existsAfter = s3After.contents().stream().anyMatch(obj -> obj.key().equals(recentFileName));
        assertThat(existsAfter).isTrue();
    }

    @Test
    void shouldHardDeleteExpiredFolders() {
        Folder expiredFolder = new Folder();
        expiredFolder.setName("old folder");
        expiredFolder.addOwner(testPhotographer);
        expiredFolder = folderRepository.save(expiredFolder);
        Long folderId = expiredFolder.getId();

        jdbcTemplate.update("UPDATE tb_folders SET deleted_at = CURRENT_TIMESTAMP - INTERVAL '40 days' WHERE id = ?", folderId);

        storageCleanupScheduler.cleanupExpiredStorage();

        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM tb_folders WHERE id = ?",
                Integer.class,
                folderId
        );
        assertThat(dbCount).isEqualTo(0);
    }


}
