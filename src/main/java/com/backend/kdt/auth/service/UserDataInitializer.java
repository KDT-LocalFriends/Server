package com.backend.kdt.auth.service;

import com.backend.kdt.auth.entity.Age;
import com.backend.kdt.auth.entity.Gender;
import com.backend.kdt.auth.entity.User;
import com.backend.kdt.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return; // 이미 데이터가 있다면 실행하지 않음

        List<User> users = List.of(
                // 테스트 유저 1
                User.builder()
                        .userName("test123")
                        .password(passwordEncoder.encode("test123"))
                        .gender(Gender.MALE)
                        .age(Age.TEENS_20S)
                        .build(),

                // 테스트 유저 2
                User.builder()
                        .userName("user456")
                        .password(passwordEncoder.encode("user456"))
                        .gender(Gender.FEMALE)
                        .age(Age.THIRTIES_FOURTIES)
                        .build(),

                // 테스트 유저 3
                User.builder()
                        .userName("demo789")
                        .password(passwordEncoder.encode("demo789"))
                        .gender(Gender.MALE)
                        .age(Age.FIFTY_PLUS)
                        .build()
        );

        userRepository.saveAll(users);
        System.out.println("테스트 유저 3명 생성 완료:");
        System.out.println("1. test123 / test123 (남성, 10-20대)");
        System.out.println("2. user456 / user456 (여성, 30-40대)");
        System.out.println("3. demo789 / demo789 (남성, 50대 이상)");
    }
}