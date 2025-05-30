package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {

    private Long id;
    private Set<Long> taskLabelIds;
    private Integer index;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonAlias("assignee_id")
    private Long assigneeId;

    private String title;
    private String content;
    private String status;
}
