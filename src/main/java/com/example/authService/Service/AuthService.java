package com.example.authService.Service;

import com.example.authService.Dto.JwtResponse;
import com.example.authService.Dto.LoginDto;
import com.example.authService.Dto.RegisterDto;
import com.example.authService.Dto.TokenRefreshRequest;
import com.example.authService.Entity.RefreshToken;
import com.example.authService.Entity.Role;
import com.example.authService.Entity.User;
import com.example.authService.Exception.EmailAlreadyExistsException;
import com.example.authService.Exception.TokenRefreshException;
import com.example.authService.Exception.UserNotFoundException;
import com.example.authService.Mapper.UserMapper;
import com.example.authService.Repository.RefreshTokenRepository;
import com.example.authService.Repository.UserRepository;
import com.example.authService.Security.JwtCore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void register(RegisterDto registerDto){

        if(userRepository.findUserByEmail(registerDto.email()).isPresent()){
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user=mapper.toUser(registerDto);
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        user.getRoles().add(Role.ROLE_USER);

        userRepository.save(user);
    }

    @Transactional
    public void addRoleToUser(UUID id,Role newRole){

            User userToUpdate=userRepository.findById(id).
                    orElseThrow(()-> new UserNotFoundException("Пользователь не найден"));
            userToUpdate.getRoles().add(newRole);
    }

    @Transactional
    public JwtResponse login(LoginDto loginDto){

        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.email(),loginDto.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken=jwtCore.generateToken(userDetails);

        User user=(User) userDetails;
        RefreshToken refreshToken=refreshTokenService.createRefreshToken(user.getId());

        return new JwtResponse(accessToken,refreshToken.getToken());
    }

    @Transactional()
    public JwtResponse refreshToken(TokenRefreshRequest refreshRequest){
        RefreshToken refreshToken=refreshTokenRepository.findByToken(refreshRequest.refreshToken())
                .orElseThrow(()->new TokenRefreshException("Токен не найден"));

        RefreshToken checkedToken=refreshTokenService.verifyExpiration(refreshToken);

        User user=refreshToken.getUser();
        String newAccessToken= jwtCore.generateToken(user);

        return new JwtResponse(newAccessToken,refreshToken.getToken());
    }
}
