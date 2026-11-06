package cn.danxing.platform.model;
import java.io.Serializable;
import java.time.*;
import java.util.Map;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import cn.danxing.platform.JsonMapHandler;
@TableName(value="calendar_entries", autoResultMap=true)
public class CalendarEntry implements Serializable {
    @TableId(type=IdType.INPUT)
    public String id;
    public String ownerId;
    public String title;
    public LocalDate startDate;
    public LocalDate endDate;
    public String category;
    public String note;
    public String recordId;
    public Instant createdAt;
    public Instant updatedAt;
}
