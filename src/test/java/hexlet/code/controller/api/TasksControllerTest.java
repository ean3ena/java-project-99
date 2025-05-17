package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import jakarta.transaction.Transactional;
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
class TasksControllerTest {

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
    private TaskMapper taskMapper;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;
    private TaskStatus testTaskStatus;
    private Label testLabel;
    private Task testTask;

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

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(testTaskStatus);

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setTaskIndex(2);
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testTaskStatus);
        testTask.addLabel(testLabel);
        taskRepository.save(testTask);
    }

    @Test
    void testIndex() throws Exception {

        var request = get("/api/tasks").with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskDTO> usersDTO = taskRepository.findAll().stream()
                .map(taskMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testIndexWithTitleCont() throws Exception {

        var taskName = testTask.getName();
        var titleSubString = taskName.substring(2, 10);

        var request = get("/api/tasks?titleCont=" + titleSubString).with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskDTO> usersDTO = taskRepository.findAll().stream()
                .filter(task -> task.getName().contains(titleSubString))
                .map(taskMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testIndexWithAssigneeId() throws Exception {

        var assignee = testTask.getAssignee();
        var assigneeId = assignee.getId();

        var request = get("/api/tasks?assigneeId=" + assigneeId).with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskDTO> usersDTO = taskRepository.findAll().stream()
                .filter(task -> task.getAssignee().getId().equals(assigneeId))
                .map(taskMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testIndexWithStatus() throws Exception {

        var status = testTask.getTaskStatus();
        var statusSlug = status.getSlug();

        var request = get("/api/tasks?status=" + statusSlug).with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskDTO> usersDTO = taskRepository.findAll().stream()
                .filter(task -> task.getTaskStatus().getSlug().equals(statusSlug))
                .map(taskMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    @Transactional
    void testIndexWithLabelId() throws Exception {

        var labelId = testLabel.getId();

        var request = get("/api/tasks?labelId=" + labelId).with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<TaskDTO> usersDTO = taskRepository.findAll().stream()
                .filter(task -> {
                    for (var label : task.getLabels()) {
                        if (label.getId().equals(labelId)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(taskMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testShow() throws Exception {

        var request = get("/api/tasks/" + testTask.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testTask.getName()))
                .andExpect(jsonPath("$.content").value(testTask.getDescription()));
    }

    @Test
    void testCreate() throws Exception {

        var labelData = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(labelData);

        var taskData = Instancio.of(modelGenerator.getTaskModel()).create();
        taskData.setTaskIndex(3);
        taskData.setAssignee(testUser);
        taskData.setTaskStatus(testTaskStatus);
        taskData.addLabel(labelData);

        var taskCreateDTO = taskMapper.map(taskData);

        String taskJson = objectMapper.writeValueAsString(taskCreateDTO);

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findById(testTask.getId() + 1).orElse(null);

        assertNotNull(task);
        assertEquals(taskData.getTaskIndex(), task.getTaskIndex());
        assertEquals(taskData.getName(), task.getName());
        assertEquals(taskData.getDescription(), task.getDescription());
    }

    @Test
    void testCreateWhereUserIsNull() throws Exception {

        var labelData = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(labelData);

        var taskData = Instancio.of(modelGenerator.getTaskModel()).create();
        taskData.setTaskIndex(3);
        taskData.setAssignee(null);
        taskData.setTaskStatus(testTaskStatus);
        taskData.addLabel(labelData);

        var taskCreateDTO = taskMapper.map(taskData);

        String taskJson = objectMapper.writeValueAsString(taskCreateDTO);

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findById(testTask.getId() + 1).orElse(null);

        assertNotNull(task);
        assertEquals(taskData.getTaskIndex(), task.getTaskIndex());
        assertEquals(taskData.getName(), task.getName());
        assertEquals(taskData.getDescription(), task.getDescription());
    }

    @Test
    void testUpdate() throws Exception {

        var data = new HashMap<String, String>();
        data.put("title", "someName");

        var dataJson = objectMapper.writeValueAsString(data);

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataJson);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var task = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals("someName", task.getName());
    }

    @Test
    void testDestroyTask() throws Exception {

        var taskId = testTask.getId();

        var request = delete("/api/tasks/" + taskId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var task = taskRepository.findById(taskId).orElse(null);

        assertNull(task);
    }

    @Test
    void testDestroyUserAssociatedWithTask() throws Exception {

        var userId = testUser.getId();

        var request = delete("/api/users/" + userId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        var user = userRepository.findById(userId).orElse(null);

        assertNotNull(user);
    }
}
