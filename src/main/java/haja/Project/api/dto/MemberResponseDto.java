package haja.Project.api.dto;

import haja.Project.domain.Authority;
import haja.Project.domain.Image;
import haja.Project.domain.Member;
import haja.Project.domain.Part;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDto {
    private String email;

    public static MemberResponseDto of(Member member) {
        return new MemberResponseDto(member.getEmail());
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MemberInfo {
        Long id;
        String email;
        Authority authority;
        String phone_num;
        Part part;
        String name;
        String comment;
        String major;
        String student_id;
        Image image;
        Long accessTokenExpiresIn;

        public static MemberInfo from(Member member) {
            return new MemberInfo(
                    member.getId(),
                    member.getEmail(),
                    member.getAuthority(),
                    member.getPhone_num(),
                    member.getPart(),
                    member.getName(),
                    member.getComment(),
                    member.getMajor(),
                    member.getStudent_id(),
                    member.getImage(),
                    member.getAccessTokenExpiresIn()==null?null:Duration.between(LocalDateTime.now(), member.getAccessTokenExpiresIn()).toMinutes()
            );
        }
    }
}
