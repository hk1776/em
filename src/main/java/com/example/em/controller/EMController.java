package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.repository.MemberRepository;
import com.example.em.service.EMService;
import com.example.em.service.LoginService;
import com.example.em.service.MemberService;
import com.example.em.service.PostService;
import com.example.em.session.SessionConst;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/em")
public class EMController {
    private final PostService postService;
    private final MemberService memberService;
    private final EMService emService;


    @GetMapping()
    public String home(@Login Member loginmember, Model model) {

        if (loginmember == null) {
            return "layouts/login";
        }
        log.info("로그인 멤버: "+loginmember.getName());
        model.addAttribute("member", loginmember);
        return "layouts/home";
    }

    @PostMapping("/info")
    public String getInfo(@Login Member loginmember,EMDto.PostInfo info, Model model) {
        if (loginmember == null) {
            return "layouts/login";
        }

        String url = "Azure AI model Server URL";
        String send = postService.sendPostRequest(url, info);
        Gson gson = new Gson();
        EMDto.Info data = gson.fromJson(send, EMDto.Info.class);
        System.out.println(data);
        List<EMDto.Hospital> hospitalList = emService.transformData(data);
        boolean emergency = true;

        if(!hospitalList.isEmpty()) {
            emergency = false;
            for(int i = 0;i<hospitalList.size();i++) {
                model.addAttribute("data"+i, hospitalList.get(i));
            }

        }
        log.info("등급"+data.getEm_class());
        model.addAttribute("class", data.getEm_class());
        model.addAttribute("emergency", emergency);
        model.addAttribute("member", loginmember);
        return "layouts/result";
    }
    @PostMapping("/info/detail")
    public String detail(@Login Member loginmember,EMDto.Hospital data, Model model) {
        if (loginmember == null) {
            return "layouts/login";
        }
        log.info(data.toString());
        model.addAttribute("member", loginmember);
        model.addAttribute("data", data);
        return "layouts/detail";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("lat2") String lat,
                                   @RequestParam("lon2") String lon,
                                   Model model,
                                   @Login Member loginmember) {
        if (loginmember == null) {
            return "layouts/login";
        }
        if (!file.getContentType().equals("audio/mp3")) {
            log.info("파일 형식 에러"+file.getContentType());
        }

        // lat와 lon을 "lat,lon" 문자열로 결합
        String location = lat + "," + lon;
        log.info("latlon"+location);
        // FastAPI 서버로 파일 전송
        String fastApiUrl = "Azure AI model Server URL";;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Request body에 파일과 위치 데이터(emer) 추가
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("emer", location);  // lat, lon 결합된 문자열을 'emer' 파라미터로 전송

        // HTTP 요청 생성
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);
        String responseBody = response.getBody();

        log.info("음성 응답"+responseBody.toString());

        // 받은 JSON 결과를 Java 객체로 변환
        Gson gson = new Gson();
        EMDto.Info data = gson.fromJson(responseBody, EMDto.Info.class);
        System.out.println(data);

        // 데이터를 병원 리스트로 변환
        List<EMDto.Hospital> hospitalList = emService.transformData(data);

        boolean emergency = true;  // 기본적으로 응급 상황으로 설정
        if (!hospitalList.isEmpty()) {
            emergency = false;  // 병원 목록이 존재하면 응급 상황이 아님
            // 병원 데이터를 모델에 추가
            for (int i = 0; i < hospitalList.size(); i++) {
                model.addAttribute("data" + i, hospitalList.get(i));
            }
        }

        // 응급 상황 여부와 로그인된 회원 정보 모델에 추가
        model.addAttribute("emergency", emergency);
        model.addAttribute("member", loginmember);
        return "layouts/result";  // 응급 상황 처리 결과 화면으로 이동

    }
}
