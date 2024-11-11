package com.web.mvc.security;

import com.web.mvc.domain.Member;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Getter
public class CustomMemberDetails implements UserDetails{

    private final Member member;

    public CustomMemberDetails(Member member) {
        this.member = member;
        log.info("CustomMemberDetails : {}", member);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("getAuthorities...");

        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(()->member.getRole());// ROLE_xxx 저장

        return collection;
    }

    @Override
    public String getPassword()
    {
        log.info("getPassword...");
        return member.getPwd();
    }

    @Override
    public String getUsername() {// 여기서의 username 은 이름이 아니라 username
        log.info("getUsername...");
        return member.getName();
    }

    /*
    밑에 코드들 자동으로 호출된다
     */
    @Override
    public boolean isAccountNonExpired() {
        log.info("isAccountNonExpired...");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        log.info("isAccountNonLocked...");
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        log.info("isCredentialsNonExpired...");
        return true;
    }

    @Override
    public boolean isEnabled() {
        log.info("isEnabled...");
        return true;
    }
}
