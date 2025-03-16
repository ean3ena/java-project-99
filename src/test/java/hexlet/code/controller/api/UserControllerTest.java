package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.controller.util.ModelGenerator;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import jakarta.validation.ValidationException;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    private User testUserId2;

    private User testUserId3;

    @BeforeEach
    public void setUp() {
        testUserId2 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUserId2);

        testUserId3 = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUserId3);
    }

    @AfterEach
    public void resetDb() {
        userRepository.deleteAll();
    }

    @Test
    public void testIndex() throws Exception {

        var request = get("/api/users");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(
                        om.writeValueAsString(userService.getAll())
                ));
    }

    @Test
    public void testShow() throws Exception {

        var request = get("/api/users/{id}", testUserId2.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(testUserId2.getFirstName()))
                .andExpect(jsonPath("$.email").value(testUserId2.getEmail()));
    }

    @Test
    public void testShowNotFoundById() throws Exception {

        var request = get("/api/users/999");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(mvcResult ->
                        assertInstanceOf(ResourceNotFoundException.class, mvcResult.getResolvedException()));
    }

    @Test
    public void testCreate() throws Exception {

        var userCreateDTO = Instancio.of(modelGenerator.getUserCreateModel()).create();

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value(userCreateDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userCreateDTO.getLastName()))
                .andExpect(jsonPath("$.email").value(userCreateDTO.getEmail()));
    }

    @Test
    public void testUpdate() throws Exception {

        var emailBeforeUpdate = testUserId3.getEmail();
        var passwordBeforeUpdate = testUserId3.getPasswordDigest();

        var data = new HashMap<>();
        data.put("email", "new-email@mail.com");
        data.put("password", "new-password");

        var request = put("/api/users/{id}", testUserId3.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var testUser = userRepository.findById(testUserId3.getId()).get();
        assertThat(testUser.getEmail()).isNotEqualTo(emailBeforeUpdate);
        assertThat(testUser.getPasswordDigest())
                .isNotEqualTo(passwordBeforeUpdate);
    }

    @Test
    public void testUpdateNotValidEmail() throws Exception {

        var data = new HashMap<>();
        data.put("email", "not-valid$email.com");

        var request = put("/api/users/{id}", testUserId3.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(mvcResult ->
                        mvcResult.getResolvedException().getClass()
                                .equals(ValidationException.class));
    }

    @Test
    public void testDestroy() throws Exception {

        var testId = testUserId2.getId();

        var request = delete("/api/users/{id}", testId);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testId)).isFalse();
    }

    @Test
    public void testDestroyNotFoundById() throws Exception {

        var request = delete("/api/users/999");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(mvcResult ->
                        assertInstanceOf(ResourceNotFoundException.class, mvcResult.getResolvedException()));
    }

}
