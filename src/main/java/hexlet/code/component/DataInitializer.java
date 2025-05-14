package hexlet.code.component;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        initUser();

        initTaskStatus("Draft", "draft");
        initTaskStatus("To Review", "to_review");
        initTaskStatus("To Be Fixed", "to_be_fixed");
        initTaskStatus("To Publish", "to_publish");
        initTaskStatus("Published", "published");

        initLabel("feature");
        initLabel("bug");
    }

    private void initUser() {

        var user = userRepository.findByEmail("hexlet@example.com");

        if (user.isEmpty()) {
            var userData = new UserCreateDTO();
            userData.setFirstName("hexlet");
            userData.setEmail("hexlet@example.com");
            userData.setPassword("qwerty");
            userService.create(userData);
        }
    }

    private void initTaskStatus(String name, String slug) {

        var taskStatusInRepository = taskStatusRepository.findBySlug(slug);

        if (taskStatusInRepository.isEmpty()) {
            var taskStatus = new TaskStatus();
            taskStatus.setName(name);
            taskStatus.setSlug(slug);
            taskStatusRepository.save(taskStatus);
        }
    }

    private void initLabel(String name) {

        var labelInRepository = labelRepository.findByName(name);

        if (labelInRepository.isEmpty()) {
            var label = new Label();
            label.setName(name);
            labelRepository.save(label);
        }
    }
}
