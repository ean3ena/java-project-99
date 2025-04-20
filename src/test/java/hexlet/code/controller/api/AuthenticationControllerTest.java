package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginTest() throws Exception {

        var authRequestDTO = new AuthRequestDTO();
        authRequestDTO.setUsername("hexlet@example.com");
        authRequestDTO.setPassword("qwerty");

        var authRequestJson = objectMapper.writeValueAsString(authRequestDTO);

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }
}