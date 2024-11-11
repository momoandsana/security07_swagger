package com.web.mvc.security;

import com.web.mvc.domain.Member;
import com.web.mvc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
    인증처리 서비스 - repository 에서 정보를 찾아서 인증
     */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override// UserDetails 는 인터페이스, 반환할 때는 구현체로 반환해야 함
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("username ={}", username); // 아이디

        //디비에 아이디에 해당하는 정보가 있는지 찾기,
        Member findMember = memberRepository.findById(username);

        if(findMember!=null){
            log.info("맴버 찾음 username ={}", username);
            return new CustomMemberDetails(findMember); // UserDetails 전달jw
        }

        return null;
    }
}
