package cn.danxing.platform;
import cn.danxing.platform.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.mockito.Mockito.*;
import java.util.List;

@WebMvcTest({AuthController.class,PlanController.class})
@org.springframework.test.context.ContextConfiguration(classes={AuthController.class,PlanController.class,ApiErrors.class,SecurityConfig.class})
class SecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean AppUserMapper users;
    @MockitoBean AppRoleService roles;
    @MockitoBean CurrentUser current;
    @MockitoBean AuditService audit;
    @MockitoBean PlanService plans;
    @MockitoBean JdbcTemplate jdbc;
    @MockitoBean PasswordEncoder encoder;
    @MockitoBean FileService files;
    @Test void unauthenticatedCannotReadPlans()throws Exception{mvc.perform(get("/api/plans")).andExpect(status().isUnauthorized());}
    @Test void csrfEndpointIsPublic()throws Exception{mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());}
    @Test void missingCsrfRejected()throws Exception{mvc.perform(post("/api/plans").with(user("c").roles("COUNSELOR")).contentType("application/json").content("{}" )).andExpect(status().isForbidden());}
    @Test void counselorCannotReadAccounts()throws Exception{mvc.perform(get("/api/admin/users").with(user("c").roles("COUNSELOR"))).andExpect(status().isForbidden());}
    @Test void counselorAdminCanReadAccounts()throws Exception{when(users.selectList(any())).thenReturn(List.of());mvc.perform(get("/api/admin/users").with(user("a").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("PERM_MANAGE_USERS")))).andExpect(status().isOk());}
    @Test void malformedPlanRejected()throws Exception{mvc.perform(post("/api/plans").with(user("c").roles("COUNSELOR")).with(csrf()).contentType("application/json").content("{\"title\":\"\",\"domain\":\"test\",\"tasks\":[]}" )).andExpect(status().isBadRequest());}
    @Test void logoutNeedsCsrf()throws Exception{mvc.perform(post("/api/auth/logout").with(user("c"))).andExpect(status().isForbidden());}
}
