package com.web.mvc.config;


import com.web.mvc.jwt.JWTFilter;
import com.web.mvc.jwt.JWTUtil;
import com.web.mvc.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    //AuthenticationManager 가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;// 주입(jwt 토큰으로 주입)

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        log.info("BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("SecurityFilterChain");
        http
                .csrf((auth) -> auth.disable())
                /*
                csrf 공격은 주로 인증쿠키를 이용하는데 jwt 기반 인증에서는 authorization 헤더에
                jwt 토큰을 포함하여 요청하기 때문에 브라우저가 자동으로 쿠키를 첨부하지 않는다
                그래서 여기에서 disable 한다
                */
                .formLogin((auth) -> auth.disable())
                /*
                스프링이 제공하는 폼을 안 쓴다, UsernamePasswordAuthenticatorFilter 을 직접 커스텀해서
                사용해야 한다
                */
                .httpBasic((auth) -> auth.disable());
        /*
        http 기본 인증은 사용자 자격 증명을 매번 클라이언트에 보낼 때 암호화되지 않은 형태로
        보내게 되기 때문에 보안적인 문제가 있음
        그리고 어차피 나중에 jwt 사용할 예정이기 때문에 비활성화함
         */

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/test", "/members", "/members/**", "/boards").permitAll()
                .requestMatchers("/admin").hasRole("ADMIN")// /admin 요청을 하기 위해서는 ADMIN 권한을 가지고 있어야 한다
                .anyRequest().authenticated());

        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를넣어야 함)
        //addFilterAt 은 UsernamePasswordAuthenticationFilter 의 자리에 LoginFilter 가 실행되도록 설정하는 것
        http.addFilterAt(new LoginFilter(
                this.authenticationManager(authenticationConfiguration) // AuthenticationManager
                        , jwtUtil), //JWTUtil
                UsernamePasswordAuthenticationFilter.class);// LoginFilter 가 이 클래스 상속 받음

        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);// 먼저 실행되는 필터
        return http.build();
        /*
        /members/* 는 하나의 세그먼트만 가능->/members/1 같이
        /members/** 는 하나 이상도 가능-> /members/1/details
         */

    }
    /*
    https 요청의 헤더에 jwt 토큰이 들어가고
    jwt 토큰에는 헤더, 페이로드, 서명이 들어간다
    서버에서는 비밀키+헤더+페이로드 조합으로 서명을 발급
    다시 요청을 받을 때는 비밀키를 사용해서 헤더+페이로드를 사용한 서명을 다시 만들고 전달받은 토큰과 비교함
    클라이언트는 비밀키를 모르기 때문에 서명을 만들 수가 없다

    셔명=해시(비밀키+헤더+페이로드)
     */
}
