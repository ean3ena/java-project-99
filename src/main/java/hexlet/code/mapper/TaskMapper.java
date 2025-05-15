package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "status")
    @Mapping(target = "labels", source = "taskLabelIds")
    @Mapping(target = "taskIndex", source = "index")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels")
    @Mapping(target = "index", source = "taskIndex")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus", source = "status")
    @Mapping(target = "labels", source = "taskLabelIds")
    @Mapping(target = "taskIndex", source = "index")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract void update(@MappingTarget Task model, TaskUpdateDTO dto);

    public TaskStatus toEntity(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow();
    }

    public Set<Label> toEntity(Set<Long> labelIds) {
        return labelIds.stream()
                .filter(id -> labelRepository.findById(id).isPresent())
                .map(id -> labelRepository.findById(id).get())
                .collect(Collectors.toSet());
    }

    public Set<Long> toDto(Set<Label> labels) {
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }
}
