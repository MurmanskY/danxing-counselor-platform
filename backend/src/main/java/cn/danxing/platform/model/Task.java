package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="tasks", autoResultMap=true)
public class Task implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String planId;
    public String title;
    public Boolean done;
    public Integer sortOrder;
}
