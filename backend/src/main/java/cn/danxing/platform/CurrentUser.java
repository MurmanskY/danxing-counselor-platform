package cn.danxing.platform;
import cn.danxing.platform.mapper.AppUserMapper;
import cn.danxing.platform.model.AppUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final AppUserMapper users;
    private final AppRoleService roles;
    public CurrentUser(AppUserMapper users, AppRoleService roles){this.users=users;this.roles=roles;}
    public AppUser get(){
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null)throw ApiException.unauthorized();
        AppUser u=users.selectOne(new QueryWrapper<AppUser>().eq("username",auth.getName()));
        if(u==null)throw ApiException.unauthorized();
        return roles.enrich(u);
    }
    public String id(){return get().id;}
}
