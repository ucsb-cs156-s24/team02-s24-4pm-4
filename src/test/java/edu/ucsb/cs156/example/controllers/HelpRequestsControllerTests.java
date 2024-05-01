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

        // Tests for GET /api/HelpRequest/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/HelpRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/HelpRequest/all"))
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
                MvcResult response = mockMvc.perform(get("/api/HelpRequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHRs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/HelpRequest/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/HelpRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/HelpRequest/post"))
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
                                .solved(true)
                                .build();

                when(helpRequestRepository.save(eq(hr1))).thenReturn(hr1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/HelpRequest/post?requesterEmail=achiang&teamId=Test1&tableOrBreakoutRoom=foo1&requestTime=2022-01-03T00:00:00&explanation=testing1&solved=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).save(hr1);
                String expectedJson = mapper.writeValueAsString(hr1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/helprequests?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/HelpRequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest hr1 = HelpRequest.builder()
                                .requesterEmail("achiang")
                                .teamId("Test1")
                                .tableOrBreakoutRoom("foo1")
                                .requestTime(ldt)
                                .explanation("testing1")
                                .solved(true)
                                .build(); 

                when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(hr1));

                // act
                MvcResult response = mockMvc.perform(get("/api/HelpRequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(hr1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/HelpRequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(helpRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("HelpRequest with id 7 not found", json.get("message"));
        }

        // Tests for DELETE /api/HelpRequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest hr1 = HelpRequest.builder()
                                .requesterEmail("achiang")
                                .teamId("Test1")
                                .tableOrBreakoutRoom("foo1")
                                .requestTime(ldt1)
                                .explanation("testing1")
                                .solved(true)
                                .build();

                when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(hr1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/HelpRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(15L);
                verify(helpRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_helprequest_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/HelpRequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/HelpRequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_helprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");


                HelpRequest hrOrig = HelpRequest.builder()
                                .requesterEmail("achiang@ucsb.edu")
                                .teamId("Test1")
                                .tableOrBreakoutRoom("foo1")
                                .requestTime(ldt1)
                                .explanation("testing1")
                                .solved(false)
                                .build();

                HelpRequest hrEdited = HelpRequest.builder()
                                .requesterEmail("achiang@cs.ucsb.edu")
                                .teamId("Test2")
                                .tableOrBreakoutRoom("foo2")
                                .requestTime(ldt2)
                                .explanation("testing2")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(hrEdited);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(hrOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/HelpRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                verify(helpRequestRepository, times(1)).save(hrEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_helprequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                HelpRequest hrEdited = HelpRequest.builder()
                                .requesterEmail("achiang@cs.ucsb.edu")
                                .teamId("Test2")
                                .tableOrBreakoutRoom("foo2")
                                .requestTime(ldt1)
                                .explanation("testing 2")
                                .solved(true)
                                .build();

                String requestBody = mapper.writeValueAsString(hrEdited);

                when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/HelpRequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(helpRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("HelpRequest with id 67 not found", json.get("message"));

        }
}
