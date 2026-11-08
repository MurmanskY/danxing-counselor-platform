package cn.danxing.platform;
import cn.danxing.platform.mapper.AppUserMapper;
import cn.danxing.platform.model.AppRole;
import cn.danxing.platform.model.AppUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final CurrentUser current;private final AppUserMapper users;private final PasswordEncoder encoder;private final AuditService audit;private final AppRoleService roles;private final JdbcTemplate jdbc;private final FileService files;
    public AuthController(CurrentUser c,AppUserMapper u,PasswordEncoder e,AuditService a,AppRoleService r,JdbcTemplate j,FileService f){current=c;users=u;encoder=e;audit=a;roles=r;jdbc=j;files=f;}
    @GetMapping("/auth/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken(),"headerName",token.getHeaderName());}
    @GetMapping("/auth/me") public AppUser me(){return current.get();}
    @GetMapping("/admin/users") public List<AppUser> users(){return users.selectList(new QueryWrapper<AppUser>().orderByAsc("created_at")).stream().map(roles::enrich).toList();}
    @GetMapping("/admin/roles") public List<AppRole> roles(){return roles.assignableRoles();}
    @PostMapping("/admin/users/{id}/avatar") @Transactional public AppUser uploadUserAvatar(
        @PathVariable String id,@RequestParam("file") org.springframework.web.multipart.MultipartFile file)throws Exception{
        AppUser u=users.selectById(id);
        if(u==null)throw ApiException.missing();
        validateAvatar(file);
        String previous=u.avatarFileId;
        var asset=files.upload(file,null);
        u.avatarFileId=asset.id;
        users.updateById(u);
        audit.write("管理员更新用户头像",u.id,asset.originalName);
        if(previous!=null&&!previous.equals(asset.id))files.deleteAvatarIfUnreferenced(previous);
        return roles.enrich(u);
    }
    @DeleteMapping("/admin/users/{id}/avatar") @Transactional public AppUser removeUserAvatar(@PathVariable String id){
        AppUser u=users.selectById(id);
        if(u==null)throw ApiException.missing();
        String previous=u.avatarFileId;
        if(previous!=null){
            jdbc.update("UPDATE app_users SET avatar_file_id=NULL WHERE id=?",u.id);
            u.avatarFileId=null;
            audit.write("管理员移除用户头像",u.id,"");
            files.deleteAvatarIfUnreferenced(previous);
        }
        return roles.enrich(u);
    }
    @GetMapping("/admin/counselor-profiles") public List<Map<String,Object>> counselorProfiles(){
        return jdbc.queryForList("SELECT cp.profile_id AS \"profileId\", sr.title, sr.department, cp.role_code AS \"roleCode\" FROM counselor_profiles cp JOIN source_records sr ON sr.id=cp.profile_id WHERE NOT EXISTS (SELECT 1 FROM app_users u WHERE u.profile_id=cp.profile_id AND u.role=cp.role_code) ORDER BY sr.title, cp.role_code");
    }
    @PostMapping("/admin/users") @Transactional public AppUser create(@Valid @RequestBody Requests.UserInput in){
        if(in.password().getBytes(StandardCharsets.UTF_8).length>72)throw ApiException.bad("密码 UTF-8 编码不能超过 72 字节");
        AppRole role=roles.require(in.role());
        if(!role.assignable)throw ApiException.bad("该角色不可分配");
        List<String> profileRoles=jdbc.query("SELECT role_code FROM counselor_profiles WHERE profile_id=? AND role_code=? AND NOT EXISTS (SELECT 1 FROM app_users WHERE profile_id=? AND role=?)",(rs,rowNum)->rs.getString(1),in.profileId(),in.role(),in.profileId(),in.role());
        if(profileRoles.isEmpty())throw ApiException.bad("请选择有效的辅导员画像");
        if(!in.role().equals(profileRoles.get(0)))throw ApiException.bad("所选画像的角色与账号角色不匹配");
        AppUser u=new AppUser();u.id=UUID.randomUUID().toString();u.username=in.username();u.passwordHash=encoder.encode(in.password());u.displayName=in.displayName();u.role=in.role();u.profileId=in.profileId();u.createdAt=Instant.now();
        try{users.insert(u);}catch(org.springframework.dao.DuplicateKeyException e){throw ApiException.bad("该账号已存在");}
        audit.write("创建账号",u.id,u.username);return roles.enrich(u);
    }
    @GetMapping("/profiles/avatars") public Map<String,String> profileAvatars(){
        Map<String,String> result=new LinkedHashMap<>();
        jdbc.query("SELECT profile_id, avatar_file_id FROM app_users WHERE role='COUNSELOR' AND profile_id IS NOT NULL AND avatar_file_id IS NOT NULL",(rs)->{
            String profileId=rs.getString(1);String fileId=rs.getString(2);
            if(profileId!=null&&fileId!=null)result.put(profileId,fileId);
        });
        return result;
    }
    @PostMapping("/me/avatar") @Transactional public AppUser uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file)throws Exception{
        AppUser u=current.get();
        validateAvatar(file);
        var asset=files.upload(file,null);
        String previous=u.avatarFileId;
        u.avatarFileId=asset.id;
        users.updateById(u);
        audit.write("更新头像",u.id,asset.originalName);
        if(previous!=null&&!previous.equals(asset.id))files.deleteAvatarIfUnreferenced(previous);
        return current.get();
    }
    @DeleteMapping("/me/avatar") public AppUser removeAvatar(){
        AppUser u=current.get();
        if(u.avatarFileId!=null){jdbc.update("UPDATE app_users SET avatar_file_id=NULL WHERE id=?",u.id);audit.write("移除头像",u.id,"");}
        return current.get();
    }
    private static void validateAvatar(org.springframework.web.multipart.MultipartFile file){
        String name=Objects.toString(file.getOriginalFilename(),"avatar").toLowerCase(Locale.ROOT);
        String ext=name.contains(".")?name.substring(name.lastIndexOf('.')+1):"";
        if(!Set.of("jpg","jpeg","png").contains(ext))throw ApiException.bad("头像仅支持 JPG 或 PNG 图片");
    }
}
