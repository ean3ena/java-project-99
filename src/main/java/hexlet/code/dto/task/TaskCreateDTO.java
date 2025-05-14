package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {

    private Set<Long> taskLabelIds;
    private Integer index;

    @JsonAlias("assignee_id")
    private Long assigneeId;

    private String title;
    private String content;
    private String status;
}
