package com.PhotoVault;

import com.PhotoVault.entities.Folder;
import com.PhotoVault.entities.Photographer;
import com.PhotoVault.entities.UserRole;
import com.PhotoVault.repository.FileRepository;
import com.PhotoVault.repository.FolderRepository;
import com.PhotoVault.repository.PhotographerRepository;
import com.PhotoVault.security.JwtTokenProvider;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
class PhotoUploadIntegrationTest extends BaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Photographer testPhotographer;
    private Photographer testOtherPhotographer;
    private Folder testFolder;
    private String validPhotographerJwtToken;
    private String otherPhotographerJwtToken;
    private MockMultipartFile mockFile;
    private Long fileId;
    private final String BUCKET_NAME = "photovault-test-bucket";

    @BeforeEach
    void setUp() throws Exception {
        fileRepository.deleteAll();
        folderRepository.deleteAll();
        photographerRepository.deleteAll();

        testPhotographer = new Photographer();
        testPhotographer.setName("Integration Tester");
        testPhotographer.setEmail("viniciustertulianodasilva.ar@gmail.com");

        testPhotographer.setPassword("encrypted_password");

        testPhotographer.setRole(UserRole.PHOTOGRAPHER);

        testPhotographer.setStorageQuotaBytes(5368709120L);

        testPhotographer.setStorageUsedBytes(0L);


        testOtherPhotographer = new Photographer();

        testOtherPhotographer.setName("other Photographer");

        testOtherPhotographer.setEmail("viniciustertulianodasilva@hotmail.com");

        testOtherPhotographer.setPassword("encrypted_password");

        testOtherPhotographer.setRole(UserRole.PHOTOGRAPHER);

        testOtherPhotographer.setStorageQuotaBytes(5368709120L);

        testOtherPhotographer.setStorageUsedBytes(0L);


        photographerRepository.saveAll(List.of(testPhotographer, testOtherPhotographer));


        testFolder = new Folder();

        testFolder.setName("Test Marriage");

        testFolder.addOwner(testPhotographer);

        folderRepository.save(testFolder);


        validPhotographerJwtToken = jwtTokenProvider.generateToken(testPhotographer.getEmail());

        otherPhotographerJwtToken = jwtTokenProvider.generateToken(testOtherPhotographer.getEmail());


        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream boas = new ByteArrayOutputStream();

        ImageIO.write(img, "jpg", boas);

        byte[] realImageBytes = boas.toByteArray();


        mockFile = new MockMultipartFile(

                "file",

                "photo-test.jpg",

                MediaType.IMAGE_JPEG_VALUE,

                realImageBytes

        );


        MvcResult result = mockMvc.perform(multipart("/api/folders/" + testFolder.getId() + "/files")

                        .file(mockFile)

                        .param("folderId", testFolder.getId().toString())

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andReturn();


        fileId = JsonPath.parse(

                        result.getResponse()

                                .getContentAsString())

                .read("$.id", Long.class);


    }


    @AfterEach
    void tearDown() {


    }


    @Test
    void shouldUploadFileSuccessfullyToLocalStackAndDatabase() throws Exception {


        mockMvc.perform(multipart("/api/folders/" + testFolder.getId() + "/files")

                        .file(mockFile)

                        .param("folderId", testFolder.getId().toString())

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isCreated())

                .andExpect(jsonPath("$.name").value("photo-test.jpg"))

                .andExpect(jsonPath("$.folderId").value(testFolder.getId()));


        var filesInDb = fileRepository.findAll();

        assertThat(filesInDb).hasSize(2);


        assertThat(filesInDb.getFirst().getName()).isEqualTo("photo-test.jpg");

        assertThat(filesInDb.getFirst().getFolder().getId()).isEqualTo(testFolder.getId());


        var updatedPhotographer = photographerRepository.findById(testPhotographer.getId());

        assertThat(updatedPhotographer.get().getStorageUsedBytes()).isEqualTo(filesInDb.getFirst().getSize() + filesInDb.getLast().getSize());


        ListObjectsV2Response s3Response = s3Client.listObjectsV2(ListObjectsV2Request.builder()

                .bucket(BUCKET_NAME)

                .build());

        assertThat(s3Response.contents()).isNotEmpty();


        boolean originalExists = s3Response.contents().stream()

                .anyMatch(obj -> obj.key().equals(filesInDb.getFirst().getStoredName()));

        assertThat(originalExists).isTrue();


        boolean thumbExists = s3Response.contents().stream()

                .anyMatch(obj -> obj.key().contains("_thumb.webp") || obj.key().contains(".jpg"));

        assertThat(thumbExists).as("Thumbnail deve ter sido gerado e guardado no LocalStack").isTrue();

    }


    @Test
    void shouldSoftDeleteFileSuccessfully() throws Exception {


        mockMvc.perform(delete("/api/files/" + fileId)

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isNoContent());


        Integer count = jdbcTemplate.queryForObject(

                "SELECT COUNT(1) FROM tb_files WHERE id = ? AND deleted_at IS NOT NULL",

                Integer.class,

                fileId);


        assertThat(count).isEqualTo(1);


        mockMvc.perform(get("/api/files/" + fileId + "/view")

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isNotFound());

    }


    @Test
    void shouldReturn401WhenAccessingWithoutToken() throws Exception {

        mockMvc.perform(get("/api/files/" + 999 + "/view"))

                .andExpect(status().isUnauthorized());

    }


    @Test
    void shouldReturnForbiddenWhenDeletingAnotherUsersFile() throws Exception {


        mockMvc.perform(delete("/api/files/" + fileId)

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherPhotographerJwtToken))

                .andExpect(status().isForbidden());

    }


    @Test
    void shouldReturn400WhenUploadingEmptyFile() throws Exception {

        MockMultipartFile emptyFile = new MockMultipartFile(

                "file",

                "vazio.jpg",

                MediaType.IMAGE_JPEG_VALUE,

                new byte[0]);


        mockMvc.perform(multipart("/api/folders/" + testFolder.getId() + "/files")

                        .file(emptyFile)

                        .param("folderId", testFolder.getId().toString())

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$.message").value("File cannot be empty"));

    }


    @Test
    void shouldReturn400WhenUploadingUnsupportedExtension() throws Exception {

        MockMultipartFile exeFile = new MockMultipartFile(

                "file",

                "virus.exe",

                MediaType.APPLICATION_OCTET_STREAM_VALUE,

                "malicious code".getBytes());


        mockMvc.perform(multipart("/api/folders/" + testFolder.getId() + "/files")

                        .file(exeFile)

                        .param("folderId", testFolder.getId().toString())

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isBadRequest());

    }


    @Test
    void shouldReturnPagedFilesForFolder() throws Exception {


        mockMvc.perform(get("/api/folders/" + testFolder.getId() + "/files")

                        .param("folderId", testFolder.getId().toString())

                        .param("page", "0")

                        .param("size", "10")

                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validPhotographerJwtToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.content").isArray())

                .andExpect(jsonPath("$.content[0].name").value("photo-test.jpg"))

                .andExpect(jsonPath("$.pageable").exists())

                .andExpect(jsonPath("$.totalElements").isNumber());

    }

} 