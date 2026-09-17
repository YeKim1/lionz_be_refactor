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
class TagApiControllerTest {
    @Mock TagService tagService;
    @InjectMocks TagApiController controller;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("7", "unused"));
    }
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void createsTagFromJson() throws Exception {
        when(tagService.save(any(Tag.class))).thenReturn(9L);
        mvc.perform(post("/tag").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"spring\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(9));
        verify(tagService).save(argThat(t -> t.getName().equals("spring")));
    }
    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/tag").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(tagService);
    }

}
