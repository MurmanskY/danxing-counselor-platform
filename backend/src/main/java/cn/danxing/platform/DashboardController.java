package cn.danxing.platform;

import cn.danxing.platform.model.AppUser;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final CurrentUser currentUser;
    private final AppRoleService roles;
    private final JdbcTemplate jdbc;

    public DashboardController(CurrentUser currentUser, AppRoleService roles, JdbcTemplate jdbc) {
        this.currentUser = currentUser;
        this.roles = roles;
        this.jdbc = jdbc;
    }

    @GetMapping("/home/dashboard")
    public Map<String, Object> dashboardScope() {
        AppUser user = currentUser.get();
        List<String> managedProfileIds = List.of();
        if (user.profileId != null && roles.hasPermission(user, "VIEW_TEAM_DASHBOARD")) {
            managedProfileIds = jdbc.query(
                "SELECT cp.profile_id FROM counselor_profiles cp JOIN app_roles r ON r.code=cp.role_code WHERE cp.manager_profile_id=? AND r.dashboard_scope='SELF' ORDER BY cp.profile_id",
                (rs, rowNum) -> rs.getString(1),
                user.profileId
            );
        }
        return Map.of(
            "profileId", user.profileId == null ? "" : user.profileId,
            "managedProfileIds", managedProfileIds
        );
    }
}
