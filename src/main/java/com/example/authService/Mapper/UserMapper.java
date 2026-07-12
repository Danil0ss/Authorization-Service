package com.example.authService.Mapper;

import com.example.authService.Dto.RegisterDto;
import com.example.authService.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id" ,ignore=true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "roles", ignore = true )
    User toUser(RegisterDto dto);
}
