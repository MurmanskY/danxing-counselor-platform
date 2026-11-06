package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="plans", autoResultMap=true)
public class Plan implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String ownerId;
    public String title;
    public String personId;
    public String opportunityId;
    public String domain;
    public LocalDate deadline;
    public String description;
    public String status;
    public Instant createdAt;
    public Instant updatedAt;
}
