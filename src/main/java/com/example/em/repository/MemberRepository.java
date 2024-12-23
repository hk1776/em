package com.example.em.repository;

import com.example.em.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> {
    List<Member> findByLoginId(String loginId);
}
