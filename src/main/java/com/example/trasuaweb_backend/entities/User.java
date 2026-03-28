package com.example.trasuaweb_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @Column(length = 50)
    private String roles; // Ví dụ: 'customer', 'admin'

    @Column(length = 50)
    private String status; // 'active', 'inactive', 'banned'

    @Column(name = "email_verified")
    private Boolean emailVerified;

    // Quan hệ 1-N: 1 User có nhiều Review (Tuỳ chọn: nếu cần lấy danh sách review của 1 user)
     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
     private List<Review> reviews;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (roles == null) roles = "customer";
        if (status == null) status = "active";
        if (emailVerified == null) emailVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Biến chuỗi role (VD: "customer") thành định dạng quyền của Spring
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.roles.toUpperCase()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Chỉ cho đăng nhập nếu status không phải là 'banned'
        return !"banned".equalsIgnoreCase(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "active".equalsIgnoreCase(this.status);
    }
}
