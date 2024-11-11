package com.web.mvc.jwt;

import com.google.gson.Gson;
import com.web.mvc.domain.Member;
import com.web.mvc.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/*
원래 UsernamePasswordAuthenticationFilter 이 필터를 써야 하는데
jwt 토큰 쓰기 위해 커스텀으로 LoginFilter 사용
 */
@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter{ //폼값 받는 컨트롤러 역할의 필터
	private final AuthenticationManager authenticationManager;
	
	private final JWTUtil jwtUtil;

	public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {		
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

    /*
    login 요청오면 호출되는 메소드
     */
    @Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
													throws AuthenticationException{
		//1. 클라이언트 로그인 요청시 id, password 받아서 출력
		String username=super.obtainUsername(request);//id
		String password = super.obtainPassword(request);//password
		
		log.info("username{}",username);
		log.info("password{}",password);
		
		//2. 스프링 시큐러티에서는 username, password를 검증하기 위해서 ~~token에 담는다.
		//지금은 authorization은 없어서  null 로 담았다. username, password 로 만든 토큰 생성
        /*
        여기서 null 이 반환되면 failureHandler 가 실행된다
         */
		UsernamePasswordAuthenticationToken authToken = 
				new UsernamePasswordAuthenticationToken(username, password,null);
		
		//3. token을 ~Manager에 전달...Provoder...DetailsServicve...db연결...CustomMemberDetails생성..Back/Back/...
		Authentication authentication=authenticationManager.authenticate(authToken);// 인증에 성공한 유저의 정보 principal, credentials, authorities
        //provider 호출하고 있음, CustomMemberDetails정보를 반환...
		log.info("authentication{}",authentication);
		return authentication;// 성공하면 이 값을 가지고 successfulAuthentication 으로 이동
	}//

	//로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) throws  IOException{
        response.setContentType("text/html;charset=UTF-8");
       log.info("로그인 성공 ......");
        //UserDetails 안에 잇는 principal(아이디 같은거) 꺼냄, Member 객체가 들어가 있음
        CustomMemberDetails customMemberDetails = (CustomMemberDetails) authentication.getPrincipal();


        /*
        하나의 유저가 여러개의 권한을 가질수 있기 때문에 collection으로 반환됨
        기본 제너릭이 GrantedAuthority이고 GrantedAuthority를 상속받은 자식들이 Role 이 된다
        이렇게 해서 Role을 만들어준다...MemberServiceImpl signUp에서 Role Setting(member.setRole("ROLE_USER"))
        우리는 하나의 권한만 지정했다..ROLE_USER
        */
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority(); //ROLE_USER or ROLE_ADMIN

//        //토큰생성과정...이때 password는 JWTUtil에서 안담았다.
        String token = jwtUtil.createJwt(
                customMemberDetails.getMember(), role, 1000L*60*10L);//1초*60*10 10분
        System.out.println("token = " + token);
        System.out.println("@@@@@@@@@@@@@@@@@@ getMember "+ customMemberDetails.getMember() +" @@@@@@@@@@@@@@@@@@");
        //응답할 헤더를 설정
        //베어러 뒤에 공백을 준다. 관례적인  prefix. 토큰은 헤더에 저장, "Bearer" 라는 접두어 사용
        response.addHeader("Authorization", "Bearer " + token);

        Map<String, Object> map = new HashMap<>();
        Member member= customMemberDetails.getMember();
        //map.put("memberNo",member.getMemberNo());
        map.put("memberNo",member.getMemberNo());
        map.put("id", member.getId());
        map.put("name", member.getName());
        map.put("address", member.getAddress());
        map.put("role", member.getRole());

        Gson gson= new Gson();
        String arr = gson.toJson(map);// json 으로 변환
        response.getWriter().print(arr);
    }
    //로그인 실패시 실행하는 메소드
    //CustomMemberDetailsService에서 null이 떨어지면 이곳으로 리턴..
    //응답 메세지를 Json형태로 프론트 단으로 넘기기 위해서 Gson 라이브러리 사용함.
    // 필터에서는 스프링이 자동으로 json 으로 만들어주지 않는다
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        log.info("로그인 실패... ......");
        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);

        Map<String, Object> map = new HashMap<>();
        map.put("errMsg","정보를 다시 확인해주세요.");
        Gson gson= new Gson();
        String arr = gson.toJson(map);
        response.getWriter().print(arr);

    }
	
}
 








