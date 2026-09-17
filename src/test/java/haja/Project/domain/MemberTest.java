package haja.Project.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    @Test
    void builderRetainsMemberProfile() {
        Image image = new Image("/member/img/profile.png", "profile.png", "/tmp/profile.png");
        Member member = Member.builder()
                .id(1L).email("member@example.com").password("encoded-password")
                .authority(Authority.ROLE_USER).name("홍길동").phone_num("01012345678")
                .part(Part.BE).comment("소개").major("컴퓨터공학").student_id("20230001")
                .image(image).build();

        assertThat(member).extracting("id", "email", "password", "authority", "name",
                        "phone_num", "part", "comment", "major", "student_id", "image")
                .containsExactly(1L, "member@example.com", "encoded-password", Authority.ROLE_USER,
                        "홍길동", "01012345678", Part.BE, "소개", "컴퓨터공학", "20230001", image);
    }

    @Test
    void memberSupportsProfileAndTokenStateChanges() {
        Member member = new Member();
        Image image = new Image("/profile.png", "profile.png", "/tmp/profile.png");
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 17, 12, 0);

        member.setComment("수정한 소개");
        member.setPassword("changed-password");
        member.setImage(image);
        member.setCount(3);
        member.setAccessTokenExpiresIn(expiresAt);

        assertThat(member).extracting("comment", "password", "image", "count", "accessTokenExpiresIn")
                .containsExactly("수정한 소개", "changed-password", image, 3, expiresAt);
        member.setImage(null);
        assertThat(member.getImage()).isNull();
    }
}
