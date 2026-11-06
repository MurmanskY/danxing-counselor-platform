package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="favorites", autoResultMap=true)
public class Favorite implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String ownerId;
    public String recordId;
}
