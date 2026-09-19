package haja.Project.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Tasknotice {


    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tasknotice_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")  //이거 name = "tasknotice_write"로 봐꿔야하나
    private Member member;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "tasknotice_date")
    private LocalDateTime date;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "tasknotice_deadline")
    private LocalDateTime deadline;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "tasknotice_target")
    private Part target;

    @Column(name = "tasknotice_link", columnDefinition = "TEXT")
    private String link; // String->File

    @Column(name = "tasknotice_title")
    private String title;

    @Column(name = "tasknotice_explanation", columnDefinition = "TEXT")
    private String explanation;

    /*
    // Tasknotice_Tag 와 양방향 매핑 시
    @OneToMany(mappedBy = "tasknotice")
    private List<Tasknotice_Tag> tasknotice_tags = new ArrayList<>();
    */

    @Builder
    public Tasknotice(Member member, Part target, String title, String explanation,
                      LocalDateTime date, LocalDateTime deadline, String link) {
        this.member = member;
        this.target = target;
        this.title = title;
        this.explanation = explanation;
        this.date = date;
        this.deadline = deadline;
        this.link = link;
    }
}
