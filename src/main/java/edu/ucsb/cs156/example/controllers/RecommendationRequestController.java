package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    @Operation(summary = "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> requests = recommendationRequestRepository.findAll();
        return requests;
    }

    @Operation(summary = "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest postRecommendationDate(
            @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name = "professorEmail") @RequestParam String professorEmail,
            @Parameter(name = "explanation") @RequestParam String explanation,
            @Parameter(name = "dateRequested") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name = "dateNeeded") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
            @Parameter(name = "done") @RequestParam boolean done)  
            
            throws JsonProcessingException {
        RecommendationRequest requestToPost = new RecommendationRequest();
        
        log.info("dateRequested:", dateRequested);
        log.info("dateNeeded:", dateNeeded);

        requestToPost.setRequesterEmail(requesterEmail);
        requestToPost.setProfessorEmail(professorEmail);
        requestToPost.setExplanation(explanation);
        requestToPost.setDateRequested(dateRequested);
        requestToPost.setDateNeeded(dateNeeded);
        requestToPost.setDone(done);



        RecommendationRequest savedRequest = recommendationRequestRepository.save(requestToPost);

        return savedRequest;
    }

    @Operation(summary = "Get recommendation request by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/get")
    public RecommendationRequest getRecommendationRequestById(@Parameter(name = "id") @RequestParam long id) {
        RecommendationRequest request = recommendationRequestRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));
        return request;

    }
}