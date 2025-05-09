package hexlet.code.service;

import hexlet.code.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.dto.task_status.TaskStatusDTO;
import hexlet.code.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    public ResponseEntity<List<TaskStatusDTO>> getAll() {
        var taskStatuses = taskStatusRepository.findAll();
        var result =  taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(taskStatuses.size()))
                .body(result);
    }

    public TaskStatusDTO findById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Status with id " + id + " not found"));
        var dto = taskStatusMapper.map(taskStatus);
        return dto;
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskStatusData) {
        var taskStatus = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(taskStatus);
        var dto = taskStatusMapper.map(taskStatus);
        return dto;
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Status with id " + id + " not found"));
        taskStatusMapper.update(taskStatusData, taskStatus);
        taskStatusRepository.save(taskStatus);
        var dto = taskStatusMapper.map(taskStatus);
        return dto;
    }

    public void delete(Long id) {
        taskStatusRepository.deleteById(id);
    }
}
