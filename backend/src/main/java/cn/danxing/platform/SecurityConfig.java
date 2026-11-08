package cn.danxing.platform;

import cn.danxing.platform.mapper.AppUserMapper;
import cn.danxing.platform.model.AppUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean LoginGuard loginGuard(){return new LoginGuard();}
    @Bean UserDetailsService users(AppUserMapper mapper, AppRoleService roles){return username->{
        AppUser u=mapper.selectOne(new QueryWrapper<AppUser>().eq("username",username));
        if(u==null)throw new UsernameNotFoundException("用户不存在");
        var authorities=roles.permissions(u.role).stream()
            .map(permission->new SimpleGrantedAuthority("PERM_"+permission)).toList();
        return User.withUsername(u.username).password(u.passwordHash).authorities(authorities).build();
    };}
    @Bean SecurityFilterChain security(HttpSecurity http)throws Exception{
        LoginGuard guard=loginGuard();
        return http
            .authorizeHttpRequests(a->a.requestMatchers("/api/auth/csrf","/api/auth/login","/actuator/health","/actuator/health/**").permitAll()
                .requestMatchers("/api/admin/users/**","/api/admin/roles","/api/admin/counselor-profiles").hasAuthority("PERM_MANAGE_USERS")
                .requestMatchers("/api/admin/records/**").hasAuthority("PERM_REVIEW_RECORDS")
                .requestMatchers("/api/admin/audit").hasAuthority("PERM_VIEW_AUDIT")
                .anyRequest().authenticated())
            .formLogin(f->f.loginProcessingUrl("/api/auth/login")
                .successHandler((q,r,a)->{
                    guard.onSuccess(String.valueOf(q.getParameter("username")),clientIp(q));
                    r.setContentType("application/json;charset=UTF-8");r.getWriter().write("{\"ok\":true}");})
                .failureHandler((q,r,e)->{
                    String username=String.valueOf(q.getParameter("username"));
                    guard.onFailure(username,clientIp(q));
                    r.setContentType("application/json;charset=UTF-8");
                    if(guard.blocked(username,clientIp(q))){
                        r.setStatus(429);
                        r.getWriter().write("{\"message\":\"失败次数过多，已临时锁定 10 分钟，请稍后再试\"}");
                    }else{
                        r.setStatus(401);
                        r.getWriter().write("{\"message\":\"账号或密码错误\"}");
                    }}))
            .logout(l->l.logoutUrl("/api/auth/logout").logoutSuccessHandler((q,r,a)->r.setStatus(204)))
            .requestCache(c->c.disable())
            .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->{r.setStatus(401);r.setContentType("application/json;charset=UTF-8");r.getWriter().write("{\"message\":\"请先登录\"}");})
                .accessDeniedHandler((q,r,x)->{r.setStatus(403);r.setContentType("application/json;charset=UTF-8");r.getWriter().write("{\"message\":\"权限不足或会话校验失效，请刷新后重试\"}");}))
            .build(); // Session-backed CSRF remains enabled, including login and logout.
    }
    @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> loginGuardFilter(){
        LoginGuard guard=loginGuard();
        var registration=new org.springframework.boot.web.servlet.FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter>(
            new org.springframework.web.filter.OncePerRequestFilter(){
                @Override protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest q,jakarta.servlet.http.HttpServletResponse r,jakarta.servlet.FilterChain c)throws jakarta.servlet.ServletException,java.io.IOException{
                    String username=String.valueOf(q.getParameter("username"));
                    if(guard.blocked(username,clientIp(q))){
                        r.setStatus(429);
                        r.setContentType("application/json;charset=UTF-8");
                        r.getWriter().write("{\"message\":\"失败次数过多，已临时锁定 10 分钟，请稍后再试\"}");
                        return;
                    }
                    c.doFilter(q,r);
                }
            });
        registration.addUrlPatterns("/api/auth/login");
        registration.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    static String clientIp(jakarta.servlet.http.HttpServletRequest q){
        String forwarded=q.getHeader("X-Forwarded-For");
        if(forwarded!=null&&!forwarded.isBlank())return forwarded.split(",")[0].trim();
        return q.getRemoteAddr();
    }
}
