package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {

    private JsonNullable<Set<Long>> taskLabelIds;
    private JsonNullable<Integer> index;

    @JsonAlias("assignee_id")
    private JsonNullable<Long> assigneeId;

    private JsonNullable<String> title;
    private JsonNullable<String> content;
    private JsonNullable<String> status;
}
