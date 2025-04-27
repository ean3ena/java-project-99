package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskStatusDTO {

    private Long id;
    private String name;

    @Column(unique = true)
    @NotBlank
    private String slug;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}
