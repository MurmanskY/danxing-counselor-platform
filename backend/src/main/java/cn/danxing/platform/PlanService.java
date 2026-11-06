package cn.danxing.platform;
import cn.danxing.platform.mapper.*;
import cn.danxing.platform.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class PlanService {
    private final PlanMapper plans;private final TaskMapper tasks;private final CurrentUser user;private final CatalogService catalog;private final AuditService audit;
    public PlanService(PlanMapper p,TaskMapper t,CurrentUser u,CatalogService c,AuditService a){plans=p;tasks=t;user=u;catalog=c;audit=a;}
    public record View(Plan plan,List<Task> tasks){}
    public List<View> list(){return plans.selectList(new QueryWrapper<Plan>().eq("owner_id",user.id()).orderByDesc("created_at")).stream().map(p->new View(p,tasks.selectList(new QueryWrapper<Task>().eq("plan_id",p.id).orderByAsc("sort_order")))).toList();}
    private Plan owned(String id){Plan p=plans.lock(id,user.id());if(p==null)throw ApiException.missing();return p;}
    @Transactional public Plan create(Requests.PlanInput in){
        Plan p=new Plan();p.id=UUID.randomUUID().toString();p.ownerId=user.id();p.title=in.title().trim();p.domain=in.domain();p.deadline=in.deadline();p.description=Objects.toString(in.description(),"");p.personId=catalog.optional(in.personId(),"person");p.opportunityId=catalog.optional(in.opportunityId(),"opportunity");p.status="进行中";p.createdAt=Instant.now();p.updatedAt=p.createdAt;plans.insert(p);
        int i=0;for(String title:in.tasks()){Task t=new Task();t.id=UUID.randomUUID().toString();t.planId=p.id;t.title=title.trim();t.done=false;t.sortOrder=i++;tasks.insert(t);}
        audit.write("创建发展计划",p.id,p.title);return p;
    }
    @Transactional public void edit(String id,Requests.PlanEdit in){
        owned(id);plans.update(null,new UpdateWrapper<Plan>().eq("id",id).set("title",in.title().trim()).set("deadline",in.deadline()).set("description",Objects.toString(in.description(),"")).set("updated_at",Instant.now()));audit.write("更新发展计划",id,in.title());
    }
    @Transactional public void toggle(String id,boolean done){
        Task t=tasks.selectById(id);if(t==null)throw ApiException.missing();Plan p=owned(t.planId);
        t.done=done;tasks.updateById(t);
        long left=tasks.selectCount(new QueryWrapper<Task>().eq("plan_id",p.id).eq("done",false));
        p.status=left==0?"已完成":"进行中";p.updatedAt=Instant.now();plans.updateById(p);audit.write("更新计划任务",id,done?"完成":"重新打开");
    }
    @Transactional public void delete(String id){Plan p=owned(id);plans.deleteById(id);audit.write("删除发展计划",id,p.title);}
    public static String cell(String s){String v=Objects.toString(s,"");String lead=v.stripLeading();if(!lead.isEmpty()&&"=+@-".indexOf(lead.charAt(0))>=0)v="'"+v;return "\""+v.replace("\"","\"\"")+"\"";}
    public String csv(){StringBuilder b=new StringBuilder("\uFEFF标题,领域,截止日期,状态,任务完成数,任务总数\r\n");for(View v:list()){Plan p=v.plan();b.append(cell(p.title)).append(',').append(cell(p.domain)).append(',').append(cell(Objects.toString(p.deadline,""))).append(',').append(cell(p.status)).append(',').append(v.tasks().stream().filter(t->t.done).count()).append(',').append(v.tasks().size()).append("\r\n");}return b.toString();}
}
