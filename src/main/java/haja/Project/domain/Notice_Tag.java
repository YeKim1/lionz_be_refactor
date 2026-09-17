package haja.Project.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Notice_Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_tag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Builder
    public Notice_Tag(Notice notice, Tag tag) {
        this.notice = notice;
        this.tag = tag;
    }

}
