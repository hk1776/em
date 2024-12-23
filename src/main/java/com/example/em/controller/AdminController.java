package com.example.em.controller;

import com.example.em.domain.EMData;
import com.example.em.domain.EMDto;
import com.example.em.service.EMService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final RestTemplate restTemplate;

    @Value("${hospital.api.host}")
    private String hospitalApiHost;

    @GetMapping("/admin")
    public String adminPage(Model model, @RequestParam(name="page", defaultValue = "1") int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);
        log.info("Accessing admin page");

        String url = hospitalApiHost + "/items";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        Gson gson = new Gson();
        EMDto.Log[] emLogsArray = gson.fromJson(response.getBody(), EMDto.Log[].class);
        List<EMDto.Log> emLogsList = Arrays.asList(emLogsArray);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), emLogsList.size());
        List<EMDto.Log> pageContent = emLogsList.subList(start, end);

        Page<EMDto.Log> logs = new PageImpl<>(pageContent, pageable, emLogsList.size());

        model.addAttribute("logs", logs);
        model.addAttribute("prev", pageable.previousOrFirst().getPageNumber() + 1);
        model.addAttribute("next", pageable.next().getPageNumber() + 1);
        model.addAttribute("hasPrev", logs.hasPrevious());
        model.addAttribute("hasNext", logs.hasNext());

        int currentPage = logs.getNumber();
        int totalPages = logs.getTotalPages();
        List<Map<String, Object>> pageNumbers = IntStream.range(1, totalPages + 1)
                .mapToObj(i -> {
                    Map<String, Object> pageMap = new HashMap<>();
                    pageMap.put("number", i);
                    pageMap.put("isCurrentPage", i == page);
                    return pageMap;
                })
                .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("currentPage", currentPage);

        return "layouts/adminlog";
    }

}
