package com.backend.kdt.auth.dto;

import com.backend.kdt.auth.entity.Age;
import com.backend.kdt.auth.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "사용자명은 필수입니다.")
    private String userName;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotNull(message = "나이는 필수입니다.")
    private Age age;
}