package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="source_records", autoResultMap=true)
public class SourceRecord implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String kind;
    public String title;
    public String domain;
    public String source;
    @TableField(typeHandler=JsonMapHandler.class)
    public Map<String,Object> payload;
    public String department;
    public String staffId;
    public String recordType;
}
