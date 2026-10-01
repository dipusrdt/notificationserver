package com.example.notificationservice.service;

import com.example.notificationservice.dto.DueTodoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class TodoApiClient {

    private final RestClient restClient;
    private final TokenService tokenService;

    public TodoApiClient(TokenService tokenService,
                         @Value("${notification.todo-api.base-url}") String baseUrl) {
        this.tokenService = tokenService;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public List<DueTodoResponse> getTodosDueOn(LocalDate date) {
        List<DueTodoResponse> result = restClient.get()
                .uri(uri -> uri.path("/api/internal/todos/due")
                        .queryParam("date", date.toString())
                        .build())
                .headers(h -> h.setBearerAuth(tokenService.getToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<DueTodoResponse>>() {});
        return result == null ? List.of() : result;
    }
}