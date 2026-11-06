package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import java.util.List;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="app_users", autoResultMap=true)
public class AppUser implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String username;
    @JsonIgnore
    public String passwordHash;
    public String displayName;
    public String role;
    public String profileId;
    public String avatarFileId;
    public Instant createdAt;
    @TableField(exist=false)
    public String roleName;
    @TableField(exist=false)
    public List<String> permissions;
}
