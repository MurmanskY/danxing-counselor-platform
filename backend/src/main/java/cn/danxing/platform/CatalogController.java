package cn.danxing.platform;
import cn.danxing.platform.mapper.*;
import cn.danxing.platform.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.Valid;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalog;private final ObjectMapper json;private final ProfileExtensionMapper extensions;
    private final FavoriteMapper favorites;private final CurrentUser user;private final AuditService audit;private final AuditLogMapper logs;private final JdbcTemplate jdbc;
    public CatalogController(CatalogService c,ObjectMapper j,ProfileExtensionMapper e,FavoriteMapper f,CurrentUser u,AuditService a,AuditLogMapper l,JdbcTemplate db){catalog=c;json=j;extensions=e;favorites=f;user=u;audit=a;logs=l;jdbc=db;}
    @GetMapping("/catalog") public Map<String,Object> catalog()throws Exception{
        JsonNode seed=json.readTree(new ClassPathResource("seed.json").getInputStream());
        return Map.of("records",json.readTree(catalog.recordsJson()),"extensions",extensions.selectList(null),"rules",seed.get("rules"),"checklist",seed.get("checklist"),"fieldMap",seed.get("fieldMap"),"sourceMatrix",seed.get("sourceMatrix"));
    }
    @GetMapping("/favorites") public List<String> favorites(){return favorites.selectList(new QueryWrapper<Favorite>().eq("owner_id",user.id())).stream().map(f->f.recordId).toList();}
    @PutMapping("/favorites/{id}") @Transactional public void favorite(@PathVariable String id,@Valid @RequestBody Requests.FavoriteInput in){
        catalog.require(id,null);String owner=user.id();
        if(in.active())jdbc.update("INSERT INTO favorites(id,owner_id,record_id) VALUES (?,?,?) ON CONFLICT(owner_id,record_id) DO NOTHING",owner+":"+id,owner,id);
        else favorites.delete(new QueryWrapper<Favorite>().eq("owner_id",owner).eq("record_id",id));
        audit.write(in.active()?"收藏资料":"取消收藏",id,"");
    }
    @PutMapping("/admin/records/{id}/review") @Transactional public ProfileExtension review(@PathVariable String id,@Valid @RequestBody Requests.ReviewInput in){
        catalog.require(id,null);
        if("已核验".equals(in.reviewStatus())&&(in.note()==null||in.note().isBlank()))throw ApiException.bad("标记已核验时，请填写核验依据");
        ProfileExtension e=new ProfileExtension();e.recordId=id;e.keywords=Objects.toString(in.keywords(),"");e.philosophy=Objects.toString(in.philosophy(),"");e.reviewStatus=in.reviewStatus();e.note=Objects.toString(in.note(),"");e.updatedAt=Instant.now();
        jdbc.update("INSERT INTO profile_extensions(record_id,keywords,philosophy,review_status,note,updated_at) VALUES (?,?,?,?,?,now()) ON CONFLICT(record_id) DO UPDATE SET keywords=EXCLUDED.keywords,philosophy=EXCLUDED.philosophy,review_status=EXCLUDED.review_status,note=EXCLUDED.note,updated_at=now()",id,e.keywords,e.philosophy,e.reviewStatus,e.note);
        audit.write("更新资料核验",id,e.reviewStatus);return e;
    }
    @GetMapping("/admin/audit") public List<AuditLog> logs(){return logs.selectList(new QueryWrapper<AuditLog>().orderByDesc("id").last("LIMIT 100"));}
}
