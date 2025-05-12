package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
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
class LabelsControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private Label testLabel;

    @BeforeEach
    void setUp() throws Exception {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(testLabel);

        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
    }

    @AfterEach
    void clear() {
        labelRepository.deleteAll();
    }

    @Test
    void testIndex() throws Exception {

        var request = get("/api/labels").with(jwt());

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var actual = response.getContentAsString();

        List<LabelDTO> usersDTO = labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();

        var expected = objectMapper.writeValueAsString(usersDTO);

        assertEquals(expected, actual);
    }

    @Test
    void testShow() throws Exception {

        var request = get("/api/labels/" + testLabel.getId()).with(jwt());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testLabel.getName()));
    }

    @Test
    void testCreate() throws Exception {

        var labelData = Instancio.of(modelGenerator.getLabelModel()).create();
        String labelJson = objectMapper.writeValueAsString(labelData);

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(labelJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(labelData.getName()));

        var label = labelRepository.findByName(labelData.getName()).orElse(null);

        assertNotNull(label);
    }

    @Test
    void testUpdate() throws Exception {

        var data = new HashMap<String, String>();
        data.put("name", "someName");

        var dataJson = objectMapper.writeValueAsString(data);

        var request = put("/api/labels/" + testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dataJson);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var label = labelRepository.findById(testLabel.getId()).orElseThrow();
        assertEquals("someName", label.getName());
    }

    @Test
    void testDestroy() throws Exception {

        var labelId = testLabel.getId();

        var request = delete("/api/labels/" + labelId)
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var label = labelRepository.findById(labelId).orElse(null);

        assertNull(label);
    }
}
