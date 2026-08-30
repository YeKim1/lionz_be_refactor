package haja.Project.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;

    public static <T> ApiResponse<T> from(T data) {
        return new ApiResponse<>(data);
    }
}
