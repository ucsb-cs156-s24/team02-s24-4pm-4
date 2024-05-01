package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import liquibase.pro.packaged.eq;
import lombok.With;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {
    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    //Tests for GET /api/recommendationrequests/all

    @Test
    public void logged_out_users_cannot_get_all_recommendation_requests() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles ={"USER"})
    @Test
    public void users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/all"))
                .andExpect(status().is(200));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void users_can_get_all_recommendation_requests() throws Exception {
        // arrange

        LocalDateTime expectedRequested = LocalDateTime.parse("2024-04-26T08:08:00");
        LocalDateTime expectedNeeded = LocalDateTime.parse("2024-04-27T08:08:00");

        LocalDateTime expectedRequested2 = LocalDateTime.parse("2024-04-26T08:08:00");
        LocalDateTime expectedNeeded2 = LocalDateTime.parse("2024-04-27T08:08:00");
        

        RecommendationRequest expected = new RecommendationRequest();
        expected.setId(0);
        expected.setRequesterEmail("requesterEmail");
        expected.setProfessorEmail("professorEmail");
        expected.setExplanation("explanation");
        expected.setDateRequested(expectedNeeded);
        expected.setDateNeeded(expectedRequested);
        expected.setDone(false);

        RecommendationRequest expected2 = new RecommendationRequest();
        expected2.setId(1);
        expected2.setRequesterEmail("requesterEmail2");
        expected2.setProfessorEmail("professorEmail2");
        expected2.setExplanation("explanation2");
        expected2.setDateRequested(expectedNeeded2);
        expected2.setDateNeeded(expectedRequested2);
        expected2.setDone(true);

        ArrayList<RecommendationRequest> expectedRecommendations = new ArrayList<>();
        expectedRecommendations.addAll(Arrays.asList(expected, expected2));

        when(recommendationRequestRepository.findAll()).thenReturn(expectedRecommendations);

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/all")).andExpect(status().is(200)).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRecommendations);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);

    }

    // Tests for POST /api/ucsbdiningcommons...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequests/post")).andExpect(status().is(403));
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
        LocalDateTime expectedRequested = LocalDateTime.parse("2024-04-26T08:08:00");
        LocalDateTime expectedNeeded = LocalDateTime.parse("2024-04-27T08:08:00");
        // arrange

        RecommendationRequest expected = new RecommendationRequest();
        expected.setId(0);
        expected.setRequesterEmail("requesterEmail");
        expected.setProfessorEmail("professorEmail");
        expected.setExplanation("explanation");
        expected.setDateRequested(expectedRequested);
        expected.setDateNeeded(expectedNeeded);
        expected.setDone(true);

        when(recommendationRequestRepository.save(eq(expected))).thenReturn(expected);

        // act
        MvcResult response = mockMvc.perform(post("/api/recommendationrequests/post?requesterEmail=requesterEmail&professorEmail=professorEmail&explanation=explanation&dateRequested=2024-04-26T08:08:00&dateNeeded=2024-04-27T08:08:00&done=true").with(csrf())).andExpect(status().is(200)).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).save(eq(expected));
        String expectedJson = mapper.writeValueAsString(expected);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    //Tests for GET /api/recommendationrequests/get
    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/get?id=1"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequests/get?id=123"))
                .andExpect(status().is(404));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void users_can_get_by_id() throws Exception {

        LocalDateTime expectedRequested = LocalDateTime.parse("2024-04-26T08:08:00");
        LocalDateTime expectedNeeded = LocalDateTime.parse("2024-04-27T08:08:00");

        RecommendationRequest expected = new RecommendationRequest();
        expected.setId(3L);
        expected.setRequesterEmail("requesterEmail");
        expected.setProfessorEmail("professorEmail");
        expected.setExplanation("explanation");
        expected.setDateRequested(expectedNeeded);
        expected.setDateNeeded(expectedRequested);
        expected.setDone(true);

        when(recommendationRequestRepository.findById(3L)).thenReturn(Optional.of(expected));

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/get?id=3")).andExpect(status().is(200)).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(3L);
        String expectedJson = mapper.writeValueAsString(expected);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);

    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void users_can_get_by_id_recommendation_request() throws Exception {
        // arrange

        LocalDateTime expectedRequested = LocalDateTime.parse("2024-04-26T08:08:00");
        LocalDateTime expectedNeeded = LocalDateTime.parse("2024-04-27T08:08:00");

        RecommendationRequest expected = new RecommendationRequest();
        expected.setId(2L);
        expected.setRequesterEmail("requesterEmail");
        expected.setProfessorEmail("professorEmail");
        expected.setExplanation("explanation");
        expected.setDateRequested(expectedNeeded);
        expected.setDateNeeded(expectedRequested);
        expected.setDone(true);

        when(recommendationRequestRepository.findById(2L)).thenReturn(Optional.of(expected));

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequests/get?id=2")).andExpect(status().is(200)).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(2L);
        String expectedJson = mapper.writeValueAsString(expected);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    


}