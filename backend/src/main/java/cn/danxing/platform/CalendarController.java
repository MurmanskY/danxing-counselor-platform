package cn.danxing.platform;
import cn.danxing.platform.mapper.CalendarEntryMapper;
import cn.danxing.platform.mapper.DevelopmentCalendarEventMapper;
import cn.danxing.platform.model.CalendarEntry;
import cn.danxing.platform.model.DevelopmentCalendarEvent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarEntryMapper entries;private final DevelopmentCalendarEventMapper developmentEvents;private final CurrentUser user;private final CatalogService catalog;private final AuditService audit;
    public CalendarController(CalendarEntryMapper e,DevelopmentCalendarEventMapper d,CurrentUser u,CatalogService c,AuditService a){entries=e;developmentEvents=d;user=u;catalog=c;audit=a;}
    @GetMapping public Object list(){return entries.selectList(new QueryWrapper<CalendarEntry>().eq("owner_id",user.id()).orderByAsc("start_date"));}
    @GetMapping("/events") public List<DevelopmentCalendarEvent> developmentEvents(){
        return developmentEvents.selectList(new QueryWrapper<DevelopmentCalendarEvent>()
                .ge("event_date",LocalDate.now()).orderByAsc("event_date"));
    }
    private CalendarEntry owned(String id){CalendarEntry e=entries.selectOne(new QueryWrapper<CalendarEntry>().eq("id",id).eq("owner_id",user.id()));if(e==null)throw ApiException.missing();return e;}
    private void validate(Requests.CalendarInput in){if(in.endDate().isBefore(in.startDate()))throw ApiException.bad("结束日期不能早于开始日期");catalog.optional(in.recordId(),null);}
    @PostMapping @Transactional public CalendarEntry create(@Valid @RequestBody Requests.CalendarInput in){
        validate(in);CalendarEntry e=new CalendarEntry();e.id=UUID.randomUUID().toString();e.ownerId=user.id();e.title=in.title();e.startDate=in.startDate();e.endDate=in.endDate();e.category=in.category();e.note=Objects.toString(in.note(),"");e.recordId=catalog.optional(in.recordId(),null);e.createdAt=Instant.now();e.updatedAt=e.createdAt;entries.insert(e);audit.write("创建日程",e.id,e.title);return e;
    }
    @PutMapping("/{id}") @Transactional public void edit(@PathVariable String id,@Valid @RequestBody Requests.CalendarInput in){
        validate(in);owned(id);entries.update(null,new UpdateWrapper<CalendarEntry>().eq("id",id).eq("owner_id",user.id()).set("title",in.title()).set("start_date",in.startDate()).set("end_date",in.endDate()).set("category",in.category()).set("note",Objects.toString(in.note(),"")).set("record_id",catalog.optional(in.recordId(),null)).set("updated_at",Instant.now()));audit.write("更新日程",id,in.title());
    }
    @DeleteMapping("/{id}") @Transactional public void delete(@PathVariable String id){owned(id);entries.deleteById(id);audit.write("删除日程",id,"");}
}
