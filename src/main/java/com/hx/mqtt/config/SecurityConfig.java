package com.hx.mqtt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login",
                        "/css/**",
                        "/fonts/**",
                        "/images/**",
                        "/js/**",
                        "/h2-console/**",
                        "/push/**"
                ).permitAll() // 放行登录页和静态资源
                .anyRequest().authenticated() // 其他所有请求需要认证
                .and()
                .formLogin()
                .loginPage("/login") // 指定自定义登录页路径
                .defaultSuccessUrl("/index") // 登录成功后跳转的路径
                .failureUrl("/login?error=true") // 登录失败后跳转的路径
                .and()
                .logout()
                .logoutUrl("/logout") // 退出登录的URL
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true) // 使session失效
                .deleteCookies("JSESSIONID") // 删除cookie
                .and()
                .headers()  // 关键：解决H2控制台的iframe问题
                .frameOptions().sameOrigin()
                .and()
                .csrf().disable();
    }
}