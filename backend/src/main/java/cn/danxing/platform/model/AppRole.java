package cn.danxing.platform.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.List;

@TableName("app_roles")
public class AppRole {
    @TableId(type = IdType.INPUT)
    public String code;
    public String name;
    public boolean assignable;
    public String dashboardScope;
    @TableField(exist = false)
    public List<String> permissions;
}
