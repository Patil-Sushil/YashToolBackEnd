package com.kalibyte.YashTools.auth.mapper;

import com.kalibyte.YashTools.auth.dto.request.UserRegistrationRequest;
import com.kalibyte.YashTools.auth.dto.response.UserResponse;
import com.kalibyte.YashTools.auth.entity.Role;
import com.kalibyte.YashTools.auth.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserRegistrationRequest request);

    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    default List<String> mapRoles(java.util.Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
    }
}
