package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="audit_logs", autoResultMap=true)
public class AuditLog implements Serializable {
    @TableId(type=IdType.AUTO)
    public Long id;
    public String actorId;
    public String action;
    public String targetId;
    public String detail;
    public Instant createdAt;
}
