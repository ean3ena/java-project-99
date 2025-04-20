package hexlet.code.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    @NotBlank
    private String firstName;

    private String lastName;

    @Column(unique = true)
    @Email
    @NotNull
    private String email;

    @NotNull
    @Size(min = 3)
    private String password;
}
