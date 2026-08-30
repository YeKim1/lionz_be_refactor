package haja.Project.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import haja.Project.domain.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllSearchDto {

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {
        private List<NoticeDto> notice;
        private List<TasknoticeDto> tasknotice;
        private List<TaskDto> task;
        private List<Tasknotice_TagDto> tasknotice_tag;

        public static Response of(List<Notice> notices, List<Tasknotice> tasknotices, List<Task> tasks, List<Tasknotice_Tag> tags) {
            return new Response(
                    NoticeDto.from(notices),
                    TasknoticeDto.from(tasknotices),
                    TaskDto.from(tasks),
                    Tasknotice_TagDto.from(tags)
            );
        }
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NoticeDto {
        private Long id;
        private String title;
        private String explanation;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime date;
        private Part target;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deadline;
        private List<String> tag;

        public static NoticeDto from(Notice notice) {
            return new NoticeDto(
                    notice.getId(),
                    notice.getTitle(),
                    notice.getExplanation(),
                    notice.getDate(),
                    notice.getTarget(),
                    notice.getDeadline(),
                    new ArrayList<>()
            );
        }

        public static List<NoticeDto> from(List<Notice> notices) {
            return notices.stream()
                    .map(NoticeDto::from)
                    .collect(Collectors.toList());
        }
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TasknoticeDto {
        private Long id;
        private LocalDateTime date;
        private LocalDateTime deadline;
        private Part target;
        private String title;
        private String explanation;

        public static TasknoticeDto from(Tasknotice tasknotice) {
            return new TasknoticeDto(
                    tasknotice.getId(),
                    tasknotice.getDate(),
                    tasknotice.getDeadline(),
                    tasknotice.getTarget(),
                    tasknotice.getTitle(),
                    tasknotice.getExplanation()
            );
        }

        public static List<TasknoticeDto> from(List<Tasknotice> tasknotices) {
            return tasknotices.stream()
                    .map(TasknoticeDto::from)
                    .collect(Collectors.toList());
        }
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TaskDto {
        private Long id;
        //private Member member;
        private String link;
        private String explanation;
        //private Tasknotice tasknotice;

        public static TaskDto from(Task task) {
            return new TaskDto(
                    task.getId(),
                    //task.getMember(),
                    task.getLink(),
                    task.getExplanation()
                    //task.getTasknotice()
            );
        }

        public static List<TaskDto> from(List<Task> tasks) {
            return tasks.stream()
                    .map(TaskDto::from)
                    .collect(Collectors.toList());
        }
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Tasknotice_TagDto {
        private Long id;
        //private Tasknotice tasknotice;
        //private Tag tag;

        public static Tasknotice_TagDto from(Tasknotice_Tag tasknotice_tag) {
            return new Tasknotice_TagDto(
                    tasknotice_tag.getId()
                    //tasknotice_tag.getTasknotice(),
                    //tasknotice_tag.getTag()
            );
        }

        public static List<Tasknotice_TagDto> from(List<Tasknotice_Tag> tasknotice_tags) {
            return tasknotice_tags.stream()
                    .map(Tasknotice_TagDto::from)
                    .collect(Collectors.toList());
        }
    }
}
