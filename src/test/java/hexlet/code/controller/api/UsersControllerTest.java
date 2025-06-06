package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsersControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserMapper userMapper;

    private JwtRequestPostProcessor token;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {

        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();

        userRepository.deleteAll();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    void testIndex() throws Exception {

        var request = get("/api/users").with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<UserDTO> usersDTO = userRepository.findAll().stream()
                .map(userMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testShow() throws Exception {

        var request = get("/api/users/" + testUser.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUser.getLastName()));
    }

    @Test
    void testCreate() throws Exception {

        var userData = Instancio.of(modelGenerator.getUserModel()).create();
        String userJson = objectMapper.writeValueAsString(userData);

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(userData.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userData.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userData.getLastName()));

        var user = userRepository.findByEmail(userData.getEmail()).orElse(null);

        assertNotNull(user);
    }

    @Test
    void testUpdateWithOwnUser() throws Exception {

        var data = new HashMap<String, String>();
        data.put("firstName", "someOtherName");

        var dataJson = objectMapper.writeValueAsString(data);

        var request = put("/api/users/" + testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataJson);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var user = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("someOtherName", user.getFirstName());
    }

    @Test
    void testUpdateWithAnotherUser() throws Exception {

        var anotherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(anotherUser);

        var tokenAnotherUser = jwt().jwt(builder -> builder.subject(anotherUser.getEmail()));

        var data = new HashMap<String, String>();
        data.put("firstName", "someOtherName");

        var dataJson = objectMapper.writeValueAsString(data);

        var request = put("/api/users/" + testUser.getId())
                .with(tokenAnotherUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataJson);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotEquals("someOtherName", user.getFirstName());
    }

    @Test
    void testDestroyWithOwnUserAndWithoutRelatedTask() throws Exception {

        var userId = testUser.getId();

        var request = delete("/api/users/" + userId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var user = userRepository.findById(userId).orElse(null);

        assertNull(user);
    }

    @Test
    void testDestroyWithOwnUserAndRelatedTask() throws Exception {

        var testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        var testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        var testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        testTask.addLabel(testLabel);
        taskRepository.save(testTask);

        var userId = testUser.getId();

        var request = delete("/api/users/" + userId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findById(userId).orElse(null);

        assertNotNull(user);
    }

    @Test
    void testDestroyWithAnotherUser() throws Exception {

        var anotherUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(anotherUser);

        var tokenAnotherUser = jwt().jwt(builder -> builder.subject(anotherUser.getEmail()));

        var testUserId = testUser.getId();

        var request = delete("/api/users/" + testUserId)
                .with(tokenAnotherUser);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findById(testUserId).orElse(null);

        assertNotNull(user);
    }
}
