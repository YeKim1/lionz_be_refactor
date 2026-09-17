package haja.Project.api;

import haja.Project.domain.*;
import haja.Project.service.*;
import haja.Project.repository.*;
import haja.Project.api.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ScheduleApiControllerTest {
    @Mock ScheduleService scheduleService;
    @InjectMocks ScheduleApiController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", "unused"));
    }
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    private static final String BODY = "{\"title\":\"meeting\",\"category\":\"centSche\",\"startdate\":\"2026-09-17 12:00:00\",\"enddate\":\"2026-09-17 14:00:00\"}";
    private Schedule schedule() {
        Schedule s = new Schedule(); s.setId(9L); s.setTitle("meeting");
        s.setStartdate(LocalDateTime.of(2026, 9, 17, 12, 0)); s.setEnddate(s.getStartdate().plusHours(2));
        return s;
    }
    @Test
    void createsScheduleWithParsedDatesAndCategory() throws Exception {
        when(scheduleService.save(any())).thenReturn(9L);
        mvc.perform(post("/schedule").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        ArgumentCaptor<Schedule> saved = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleService).save(saved.capture());
        assertThat(saved.getValue()).extracting("title", "category", "startdate", "enddate")
                .containsExactly("meeting", Category.centSche, schedule().getStartdate(), schedule().getEnddate());
    }
    @Test
    void returnsListAndSingleScheduleWithFormattedDates() throws Exception {
        when(scheduleService.findAll()).thenReturn(List.of(schedule()));
        when(scheduleService.findOne(9L)).thenReturn(schedule());
        mvc.perform(get("/schedule")).andExpect(status().isOk()).andExpect(jsonPath("$.schedule[0].id").value(9))
                .andExpect(jsonPath("$.schedule[0].title").value("meeting"));
        mvc.perform(get("/schedule/9")).andExpect(status().isOk())
                .andExpect(jsonPath("$.startdate").value("2026-09-17 12:00:00"))
                .andExpect(jsonPath("$.enddate").value("2026-09-17 14:00:00"));
    }
    @Test
    void returnsEmptyList() throws Exception {
        mvc.perform(get("/schedule")).andExpect(status().isOk()).andExpect(jsonPath("$.schedule").isEmpty());
    }
    @Test
    void updatesExistingSchedule() throws Exception {
        Schedule existing = schedule(); existing.setTitle("old");
        when(scheduleService.findOne(9L)).thenReturn(existing);
        when(scheduleService.save(existing)).thenReturn(9L);
        mvc.perform(put("/schedule/9").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        assertThat(existing).extracting("title", "category", "startdate", "enddate")
                .containsExactly("meeting", Category.centSche, schedule().getStartdate(), schedule().getEnddate());
        verify(scheduleService).save(existing);
    }
    @Test
    void deletesSchedule() throws Exception {
        mvc.perform(delete("/schedule/9")).andExpect(status().isOk()); verify(scheduleService).delete(9L);
    }
    @Test
    void rejectsInvalidCategoryAndDate() throws Exception {
        mvc.perform(post("/schedule").contentType(MediaType.APPLICATION_JSON).content(BODY.replace("centSche", "invalid")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/schedule").contentType(MediaType.APPLICATION_JSON).content(BODY.replace("2026-09-17 12:00:00", "bad-date")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(scheduleService);
    }

}
