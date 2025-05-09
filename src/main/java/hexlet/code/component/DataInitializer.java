package hexlet.code.component;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
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
    private TaskStatusRepository taskStatusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        var userData = new UserCreateDTO();

        userData.setFirstName("hexlet");
        userData.setEmail("hexlet@example.com");
        userData.setPassword("qwerty");

        userService.create(userData);

        initTaskStatus("Draft", "draft");
        initTaskStatus("To Review", "to_review");
        initTaskStatus("To Be Fixed", "to_be_fixed");
        initTaskStatus("To Publish", "to_publish");
        initTaskStatus("Published", "published");
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
}
