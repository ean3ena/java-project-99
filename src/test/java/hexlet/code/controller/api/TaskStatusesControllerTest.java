package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task_status.TaskStatusDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskStatusesControllerTest {

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
    private TaskStatusMapper taskStatusMapper;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private TaskStatus testTaskStatus;

    @BeforeEach
    void setUp() throws Exception {

        taskRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();

        taskStatusRepository.deleteAll();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @Test
    void testIndex() throws Exception {

        var request = get("/api/task_statuses").with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskStatusDTO> usersDTO = taskStatusRepository.findAll().stream()
                .map(taskStatusMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testShow() throws Exception {

        var request = get("/api/task_statuses/" + testTaskStatus.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testTaskStatus.getName()))
                .andExpect(jsonPath("$.slug").value(testTaskStatus.getSlug()));
    }

    @Test
    void testCreate() throws Exception {

        var taskStatusData = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        String taskStatusJson = objectMapper.writeValueAsString(taskStatusData);

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskStatusJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(taskStatusData.getName()))
                .andExpect(jsonPath("$.slug").value(taskStatusData.getSlug()));

        var taskStatus = taskStatusRepository.findBySlug(taskStatusData.getSlug()).orElse(null);

        assertNotNull(taskStatus);
    }

    @Test
    void testUpdate() throws Exception {

        var data = new HashMap<String, String>();
        data.put("name", "someName");

        var dataJson = objectMapper.writeValueAsString(data);

        var request = put("/api/task_statuses/" + testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataJson);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElseThrow();
        assertEquals("someName", taskStatus.getName());
    }

    @Test
    void testDestroyWithoutRelatedTask() throws Exception {

        var taskStatusId = testTaskStatus.getId();

        var request = delete("/api/task_statuses/" + taskStatusId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var taskStatus = taskStatusRepository.findById(taskStatusId).orElse(null);

        assertNull(taskStatus);
    }

    @Test
    void testDestroyWithRelatedTask() throws Exception {

        var testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        var testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(null);
        testTask.addLabel(testLabel);
        taskRepository.save(testTask);

        var taskStatusId = testTaskStatus.getId();

        var request = delete("/api/task_statuses/" + taskStatusId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var taskStatus = taskStatusRepository.findById(taskStatusId).orElse(null);

        assertNotNull(taskStatus);
    }
}
