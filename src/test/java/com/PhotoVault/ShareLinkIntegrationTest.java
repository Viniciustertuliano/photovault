package com.PhotoVault;

import com.PhotoVault.entities.*;
import com.PhotoVault.repository.*;
import com.PhotoVault.security.JwtTokenProvider;
import com.PhotoVault.services.EmailService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ShareLinkIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PhotographerRepository photographerRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ShareLinkRepository shareLinkRepository;

    @Autowired
    private AccessPermissionRepository accessPermissionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmailService emailService;

    private Photographer testPhotographer;
    private Client testClient;
    private Folder testFolder;
    private File testFile;
    private String photographerJwtToken;
    private String clientJwtToken;
    private String shareRequestJson;
    private MvcResult shareResult;

    @BeforeEach
    void setUp() throws Exception {
        accessPermissionRepository.deleteAll();
        fileRepository.deleteAll();
        folderRepository.deleteAll();
        clientRepository.deleteAll();
        photographerRepository.deleteAll();

        testPhotographer = new Photographer();
        testPhotographer.setName("Test Photographer");
        testPhotographer.setEmail("photographer@test.com");
        testPhotographer.setPassword("password");
        testPhotographer.setRole(UserRole.PHOTOGRAPHER);
        testPhotographer.setStorageQuotaBytes(5000000L);
        photographerRepository.save(testPhotographer);

        testClient = new Client();
        testClient.setName("Test Client");
        testClient.setEmail("client@test.com");
        testClient.setPassword("password");
        testClient.setRole(UserRole.CLIENT);
        testClient.setPhoneNumber("11999999999");
        clientRepository.save(testClient);

        testFolder = new Folder();
        testFolder.setName("Test Folder");
        testFolder.addOwner(testPhotographer);
        folderRepository.save(testFolder);

        testFile = new File();
        testFile.setName("photo_01.jpg");
        String uniqueStoredName = java.util.UUID.randomUUID() + ".jpg";
        testFile.setStoredName(uniqueStoredName);
        testFile.setPath("path/s3/"+uniqueStoredName);
        testFile.setSize(1024L);
        testFile.setContentType("image/jpeg");
        testFile.setFolder(testFolder);
        fileRepository.save(testFile);

        photographerJwtToken = jwtTokenProvider.generateToken(testPhotographer.getEmail().toString());

        clientJwtToken = jwtTokenProvider.generateToken(testClient.getEmail().toString());

        shareRequestJson = """
                {
                    "clientEmail": "client@test.com",
                    "expirationDays": 30
                }
                """;

        shareResult = mockMvc.perform(post("/api/folders/" + testFolder.getId() + "/share-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(shareRequestJson)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + photographerJwtToken))
                .andReturn();
    }

    @Test
    void shouldCreateLinkSendEmailAndGrantAccessToClient() throws Exception {

        String token = JsonPath.parse(shareResult.getResponse().getContentAsString()).read("$.token");

        verify(emailService, Mockito.times(1))
                .sendShareLinkEmail(eq("client@test.com"), eq("Test Photographer"), eq("Test Folder"), eq(token));

        boolean hadPermissionBefore = accessPermissionRepository.existsByUserAndFolderAndPermissionType(testClient, testFolder, PermissionType.VIEW);
        assertThat(hadPermissionBefore).isFalse();

        mockMvc.perform(get("/api/share-links/" + token + "/access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Folder"));

        boolean hasPermissionAfter = accessPermissionRepository.existsByUserAndFolderAndPermissionType(testClient, testFolder, PermissionType.VIEW);
        assertThat(hasPermissionAfter).isTrue();

        mockMvc.perform(get("/api/files/"+ testFile.getId() + "/download")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetShareLinksByFolder() throws Exception {
        mockMvc.perform(get("/api/folders/" + testFolder.getId() + "/share-links")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + photographerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].token").exists())
                .andExpect(jsonPath("$[0].folderId").value(testFolder.getId()));
    }

    @Test
    void shouldRevokeShareLinkAndBlockClientAccess() throws Exception {
        String token = JsonPath.parse(shareResult.getResponse().getContentAsString()).read("$.token");
        Integer linkId = JsonPath.parse(shareResult.getResponse().getContentAsString()).read("$.id");

        mockMvc.perform(delete("/api/share-links/" + linkId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + photographerJwtToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/share-links/" + token + "/access")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + clientJwtToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRenewShareLink() throws Exception {
        Integer linkId = JsonPath.parse(shareResult.getResponse().getContentAsString()).read("$.id");

        Optional<ShareLink> shareLink = shareLinkRepository.findById(Long.valueOf(linkId));

        MvcResult result = mockMvc.perform(patch("/api/share-links/" + linkId)
                        .param("additionalDays", "10")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + photographerJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folderName").value(shareLink.get().getFolder().getName()))
                .andExpect(jsonPath("$.expirationDate").exists())
                .andReturn();

        assertThat(LocalDateTime.parse(JsonPath.parse(result.getResponse().getContentAsString()).read("$.expirationDate"))).isEqualTo(shareLink.get().getExpirationDate().plusDays(10));
    }
}
