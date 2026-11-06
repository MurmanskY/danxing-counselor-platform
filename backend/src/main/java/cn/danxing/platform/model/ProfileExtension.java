package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="profile_extensions", autoResultMap=true)
public class ProfileExtension implements Serializable {
    @TableId(type=IdType.INPUT)
    public String recordId;
    public String keywords;
    public String philosophy;
    public String reviewStatus;
    public String note;
    public Instant updatedAt;
}
