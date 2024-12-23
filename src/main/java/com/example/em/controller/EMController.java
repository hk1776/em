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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        String url = "https://mini7-festapi-a063094-gnh0dffwbvbfc9hd.koreacentral-01.azurewebsites.net/items/text"; // 대상 서버의 URL
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
        model.addAttribute("data", data);
        return "layouts/detail";
    }

    @GetMapping("/signin")
    public String signIn() {
        return "layouts/signIn";
    }

    @PostMapping("/signin")
    public String save(@Valid @ModelAttribute EMDto.MemberDTO member, BindingResult bindingResult,Model model) {
        Optional<Member> find = memberService.findByLoginId(member.getLoginId());
        if (find.isPresent()) {
            log.info("id 있음");
            bindingResult.reject("signInFail", "이미 있는 아이디 입니다.");
            log.info("BindingResult has errors: {}", bindingResult.hasErrors());
            model.addAttribute("signInError", bindingResult.getAllErrors());
            return "layouts/signIn";
        }else{
            log.info("id 없음");
            memberService.save(member);
            return "redirect:/";
        }

    }
}
