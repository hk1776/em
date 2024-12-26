package com.example.em.controller;

import com.example.em.config.Login;
import com.example.em.domain.EMDto;
import com.example.em.domain.Member;
import com.example.em.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/signin")
    public String signIn() {
        return "layouts/signIn";
    }

    @PostMapping("/signin")
    public String save(@Valid @ModelAttribute EMDto.MemberDTO member, BindingResult bindingResult, Model model) {
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

    @GetMapping("/change")
    public String change(@Login Member loginmember, Model model) {
        if (loginmember == null) {
            return "layouts/login";
        }
        log.info("로그인 멤버: "+loginmember.getName());
        model.addAttribute("member", loginmember);
        return "layouts/change";
    }

    @PostMapping("/change")
    public String changeMember(@Login Member loginmember,@Valid @ModelAttribute EMDto.MemberDTO member) {
        if (loginmember == null) {
            return "layouts/login";
        }
        memberService.update(loginmember,member);
        return "redirect:/";
    }
}
