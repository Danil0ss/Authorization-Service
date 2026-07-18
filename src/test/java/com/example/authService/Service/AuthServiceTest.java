package com.example.authService.Service;

import com.example.authService.Dto.JwtResponse;
import com.example.authService.Dto.LoginDto;
import com.example.authService.Dto.RegisterDto;
import com.example.authService.Dto.TokenRefreshRequest;
import com.example.authService.Entity.RefreshToken;
import com.example.authService.Entity.Role;
import com.example.authService.Entity.User;
import com.example.authService.Exception.EmailAlreadyExistsException;
import com.example.authService.Exception.UserNotFoundException;
import com.example.authService.Mapper.UserMapper;
import com.example.authService.Repository.RefreshTokenRepository;
import com.example.authService.Repository.UserRepository;
import com.example.authService.Security.JwtCore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static com.example.authService.Entity.Role.ROLE_ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1. Подключаем расширение Mockito, чтобы работали аннотации @Mock и @InjectMocks
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // 2. Создаем "моки" (заглушки) для всех зависимостей нашего сервиса
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper mapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private  RefreshTokenService refreshTokenService;

    @Mock
    private JwtCore jwtCore;

    @Mock
    private Authentication authentication;

    @Mock
    private  RefreshTokenRepository refreshTokenRepository;

    // 3. Создаем объект тестируемого сервиса и автоматически внедряем туда созданные выше моки
    @InjectMocks
    private AuthService authService;

    @Test
    void register_Success() {
        // --- ШАГ 1: Arrange (Подготовка данных) ---

        // Создаем входные данные (DTO)
        RegisterDto registerDto = new RegisterDto("test@example.com", "plainPassword");

        // Программируем поведение мока userRepository:
        // "Когда вызовут findUserByEmail с тестовым email, верни пустой Optional" (значит, такого юзера еще нет в БД)
        when(userRepository.findUserByEmail(registerDto.email())).thenReturn(Optional.empty());

        // Создаем пустой объект User, который вернет нам маппер
        User user = new User();
        user.setRoles(new HashSet<>()); // Инициализируем пустой сет, так как сервис будет добавлять туда роль

        // Программируем поведение мока mapper:
        when(mapper.toUser(registerDto)).thenReturn(user);

        // Программируем поведение мока passwordEncoder:
        // "Когда вызовут encode с 'plainPassword', верни зашифрованную строку 'hashedPassword'"
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");


        // --- ШАГ 2: Act (Выполнение действия) ---

        // Запускаем метод, который тестируем
        authService.register(registerDto);


        // --- ШАГ 3: Assert (Проверка результатов) ---

        // Проверяем, что в сущность записался зашифрованный пароль
        assertEquals("hashedPassword", user.getPassword());

        // Проверяем, что пользователю добавилась роль ROLE_USER
        assertTrue(user.getRoles().contains(Role.ROLE_USER));

        // Проверяем, что UserRepository действительно попытался сохранить этого пользователя в БД
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void register_ThrowsException_WhenEmailAlreadyExists(){

        RegisterDto registerDto=new RegisterDto("occupied@example.com","password");

        when(userRepository.findUserByEmail(registerDto.email())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistsException.class, () -> {
            authService.register(registerDto);
        });

        verifyNoInteractions(passwordEncoder,mapper);
        verify(userRepository,never()).save(any());
    }

    @Test
    void login_Success() {

        LoginDto loginDto = new LoginDto("test@example.com", "plainPassword");

        // 1. Создаем пользователя, заполняя все 5 полей, как требует конструктор User
        User user = new User(UUID.randomUUID(), "test@example.com", "plainPassword", true, new HashSet<>());

        // Настраиваем мок Authentication, чтобы он возвращал нашего пользователя
        when(authentication.getPrincipal()).thenReturn(user);

        // 2. Настраиваем AuthenticationManager.
        // Используем any(), так как токен создается внутри метода сервиса.
        // Возвращаем сам мок authentication (сетевой уровень)
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Настраиваем генерацию Access-токена
        when(jwtCore.generateToken(user)).thenReturn("mocked-jwt-token");

        // 3. РЕШЕНИЕ ТВОЕГО ВОПРОСА: Программируем поведение RefreshTokenService
        // Создаем тестовый объект RefreshToken
        RefreshToken mockRefreshToken = new RefreshToken(UUID.randomUUID(), "mocked-refresh-token", Instant.now(), user);

        // Говорим моку: "Когда вызовут createRefreshToken с ID нашего юзера, верни mockRefreshToken"
        when(refreshTokenService.createRefreshToken(user.getId())).thenReturn(mockRefreshToken);


        // --- ШАГ 2: Act (Выполнение действия) ---

        // Вызываем метод и сохраняем возвращаемый ДТО JwtResponse
        JwtResponse response = authService.login(loginDto);


        // --- ШАГ 3: Assert (Проверка результатов) ---

        // Проверяем, что в итоговом ДТО лежат именно те токены, которые мы ожидали
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("mocked-refresh-token", response.refreshToken());
    }

    @Test
    void addRoleToUser_Success(){

        UUID id=UUID.randomUUID();
        User user=new User(id,"test@example.com", "plainPassword", true, new HashSet<>());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        authService.addRoleToUser(id, Role.ROLE_ADMIN);

        assertTrue(user.getRoles().contains(ROLE_ADMIN));
    }

    @Test
    void addRoleToUser_ThrowsException_WhenUserNotFound(){
        UUID id=UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->{
            authService.addRoleToUser(id, Role.ROLE_ADMIN);
        });
    }

    @Test
    void refreshToken_Success(){

        TokenRefreshRequest refreshRequest=new TokenRefreshRequest("refreshToken");
        User user = new User(UUID.randomUUID(), "test@example.com", "plainPassword", true, new HashSet<>());
        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "refreshToken", Instant.now(), user);

        when(refreshTokenRepository.findByToken(refreshRequest.refreshToken())).
                thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(jwtCore.generateToken(user)).thenReturn("newToken");

        JwtResponse response= authService.refreshToken(refreshRequest);

        assertEquals("newToken",response.token());

    }
}