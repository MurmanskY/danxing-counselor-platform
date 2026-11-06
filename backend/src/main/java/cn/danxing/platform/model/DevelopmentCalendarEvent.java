package cn.danxing.platform.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("development_calendar_events")
public class DevelopmentCalendarEvent {
    @TableId
    public String id;
    public String title;
    public LocalDate eventDate;
    public String category;
}
