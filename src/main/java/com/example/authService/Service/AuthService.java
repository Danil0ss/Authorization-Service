package com.example.authService.Service;

import com.example.authService.Dto.RegisterDto;
import com.example.authService.Entity.Role;
import com.example.authService.Entity.User;
import com.example.authService.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterDto registerDto){

        if(userRepository.findUserByEmail(registerDto.email()).isPresent()){
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user=new User();
        user.setEmail(registerDto.email());
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        user.getRoles().add(Role.ROLE_USER);

        userRepository.save(user);
    }

    @Transactional
    public void addRoleToUser(UUID id,Role newRole){

            User userToUpdate=userRepository.findById(id).
                    orElseThrow(()-> new RuntimeException("Пользователь не найден"));
            userToUpdate.getRoles().add(newRole);
    }
}
