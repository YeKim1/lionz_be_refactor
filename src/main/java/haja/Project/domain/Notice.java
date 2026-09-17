package haja.Project.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Notice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "notice_target")
    private Part target;

    @Column(name = "notice_title")
    private String title;

    @Column(name = "notice_explanation", columnDefinition = "TEXT")
    private String explanation;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "notice_date")
    private LocalDateTime date;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "notice_deadline")
    private LocalDateTime deadline;

    @Column(name = "notice_like")
    private Long like; //좋아요

    @Builder
    public Notice(Member member, Part target, String title, String explanation, LocalDateTime date, LocalDateTime deadline) {
        this.member = member;
        this.target = target;
        this.title = title;
        this.explanation = explanation;
        this.date = date;
        this.deadline = deadline;
    }

}
