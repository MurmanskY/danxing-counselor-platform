package cn.danxing.platform;

import cn.danxing.platform.mapper.AppRoleMapper;
import cn.danxing.platform.model.AppRole;
import cn.danxing.platform.model.AppUser;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppRoleService {
    private final AppRoleMapper roles;
    private final JdbcTemplate jdbc;

    public AppRoleService(AppRoleMapper roles, JdbcTemplate jdbc) {
        this.roles = roles;
        this.jdbc = jdbc;
    }

    public AppRole require(String code) {
        AppRole role = roles.selectById(code);
        if (role == null) throw ApiException.bad("角色不存在");
        role.permissions = permissions(code);
        return role;
    }

    public List<AppRole> assignableRoles() {
        List<AppRole> results = roles.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppRole>()
                .eq("assignable", true)
                .orderByAsc("code")
        );
        results.forEach(role -> role.permissions = permissions(role.code));
        return results;
    }

    public List<String> permissions(String code) {
        return jdbc.query(
            "SELECT permission_code FROM app_role_permissions WHERE role_code = ? ORDER BY permission_code",
            (rs, rowNum) -> rs.getString(1),
            code
        );
    }

    public AppUser enrich(AppUser user) {
        AppRole role = require(user.role);
        user.roleName = role.name;
        user.permissions = role.permissions;
        return user;
    }

    public boolean hasPermission(AppUser user, String permission) {
        return permissions(user.role).contains(permission);
    }
}
