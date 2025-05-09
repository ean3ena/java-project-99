package hexlet.code.util;

import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ModelGenerator {

    private Model<User> userModel;
    private Model<TaskStatus> taskStatusModel;
    private Model<Task> taskModel;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder encoder;

    @PostConstruct
    private void init() {

        userModel = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> encoder.encode(faker.internet().password()))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .toModel();

        taskStatusModel = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.text().text(10))
                .supply(Select.field(TaskStatus::getSlug), () -> faker.internet().slug())
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .toModel();

        taskModel = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.text().text(25))
                .supply(Select.field(Task::getDescription), () -> faker.text().text(100))
                .ignore(Select.field(Task::getIndex))
                .ignore(Select.field(Task::getTaskStatus))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getCreatedAt))
                .toModel();
    }
}
