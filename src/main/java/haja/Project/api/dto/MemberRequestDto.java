package haja.Project.api.dto;

import haja.Project.domain.Authority;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {

    private String email;
    private String password;

    //회원가입 페이지는 없고 우리가 직접 계정 넣어주기로 했으니 phone_num,part 이런거 추가없이 이대로 가는게 나을듯
    //사실 바꾸면 로그인이랑 회원가입 DTO 또 분리해서 따로 만들어야 할거같아서 그대로 ㄱㄱ
    public Member toMember(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .authority(Authority.ROLE_USER)
                .image(new Image("https://lionz.kro.kr/member/img/DefaultProfile.png","DefaultProfile.png","/home/img/DefaultProfile.png"))
                .build();

    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }

    @Data
    public static class UpdateComment {
        public String comment;
    }

    @Data
    @NotBlank
    public static class UpdatePassword {
        String password;
    }
}
