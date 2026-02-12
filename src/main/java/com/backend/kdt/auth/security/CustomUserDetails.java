package com.backend.kdt.auth.security;

import com.backend.kdt.auth.entity.User;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long userId;

    public CustomUserDetails(User user) {
        super(
                user.getUserName(),
                "",
                List.of(new SimpleGrantedAuthority("USER"))
        );
        this.userId = user.getId();
    }
}