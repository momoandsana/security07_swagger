package com.web.mvc.jwt;

import com.web.mvc.domain.Member;
import com.web.mvc.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// prg 패턴 때문에 OncePerRequestFilter 를 사용한다
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
				
        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");
				
        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            //System.out.println("token null");
            log.info("token null");
            filterChain.doFilter(request, response);// 다음 필터 호출
						
            //조건이 해당되면 메소드 종료 (필수)
            return;// 토큰이 있다면 여기로 돌아오지 않는다 doFilter 에서 진행한다
        }
			
        //System.out.println("authorization now");
        log.info("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];
			
        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            //System.out.println("token expired");
            log.info("token expired");

           

            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        //토큰에서 username과 role 획득
        String memberNo=jwtUtil.getMemberNo(token);
        String username = jwtUtil.getUsername(token);
        String id = jwtUtil.getId(token);
        String role = jwtUtil.getRole(token);
				
        //userEntity를 생성하여 값 set
        Member member = new Member();
        //member.setMemberNo(Long.valueOf(memberNo));
        member.setId(id);
        member.setName(username);
        member.setRole(role);
				
        //UserDetails에 회원 정보 객체 담기
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken =
                new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
        //세션에 사용자 등록 - 세션이 만들어짐.
        SecurityContextHolder.getContext().setAuthentication(authToken);// 토큰을 context holder 에 저장
        filterChain.doFilter(request, response);// 다음 필터 호출
    }
}