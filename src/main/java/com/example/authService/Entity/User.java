package com.example.authService.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

@Id
@GeneratedValue(strategy =GenerationType.UUID)
private UUID id;

@Column(nullable = false,unique = true,length = 100)
private String email;

@Column(nullable = false)
private String password;

@Column(nullable = false)
private Boolean isActive=true;

@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
        name ="roles",
        joinColumns = @JoinColumn(name = "user_id")
)
@Column(name = "role")
@Enumerated(EnumType.STRING)
private Set<Role> roles=new HashSet<>();
}
