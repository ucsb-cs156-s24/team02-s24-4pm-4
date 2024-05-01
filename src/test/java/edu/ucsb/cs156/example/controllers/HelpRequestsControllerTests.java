package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestsController.class)
@Import(TestConfig.class)
public class HelpRequestsControllerTests extends ControllerTestCase {

        @MockBean
        HelpRequestRepository helpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/helprequests/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_helprequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest hr1 = HelpRequest.builder()
                                .requesterEmail("achiang@ucsb.edu")
                                .teamId("Test1")
                                .tableOrBreakoutRoom("foo1")
                                .requestTime(ldt1)
                                .explanation("testing 1")
                                .solved(false)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                HelpRequest hr2 = HelpRequest.builder()
                                .requesterEmail("achiang@cs.ucsb.edu")
                                .teamId("Test2")
                                .tableOrBreakoutRoom("foo2")
                                .requestTime(ldt2)
                                .explanation("testing 2")
                                .solved(true)
                                .build();

                ArrayList<HelpRequest> expectedHRs = new ArrayList<>();
                expectedHRs.addAll(Arrays.asList(hr1, hr2));

                when(helpRequestRepository.findAll()).thenReturn(expectedHRs);

                // act
                MvcResult response = mockMvc.perform(get("/api/helprequests/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHRs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/helprequests/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequests/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/helprequests/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest hr1 = HelpRequest.builder()
                                .requesterEmail("achiang")
                                .teamId("Test1")
                                .tableOrBreakoutRoom("foo1")
                                .requestTime(ldt1)
                                .explanation("testing1")
                                .solved(false)
                                .build();

                when(helpRequestRepository.save(eq(hr1))).thenReturn(hr1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/helprequests/post?requesterEmail=achiang&teamId=Test1&tableOrBreakoutRoom=foo1&requestTime=2022-01-03T00:00:00&explanation=testing1&solved=false")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(hr1);
                String expectedJson = mapper.writeValueAsString(hr1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

}
