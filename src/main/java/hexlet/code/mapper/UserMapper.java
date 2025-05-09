package hexlet.code.mapper;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class},
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JsonNullableMapper jsonNullableMapper;

    @Mapping(target = "passwordDigest", source = "password")
    public abstract User map(UserCreateDTO dto);

    public abstract UserDTO map(User model);

    @Mapping(target = "passwordDigest", source = "password")
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);

    @BeforeMapping
    public void encryptPassword(UserCreateDTO data) {
        var password = data.getPassword();
        data.setPassword(encoder.encode(password));
    }

    @BeforeMapping
    public void encryptPassword(UserUpdateDTO data) {
        if (jsonNullableMapper.isPresent(data.getPassword())) {
            var password = data.getPassword().get();
            data.setPassword(jsonNullableMapper.wrap(encoder.encode(password)));
        }
    }
}
