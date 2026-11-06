package cn.danxing.platform;
import cn.danxing.platform.mapper.*;
import cn.danxing.platform.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
@Service
public class CatalogService {
    private final SourceRecordMapper records;private final ObjectMapper json;
    public CatalogService(SourceRecordMapper r,ObjectMapper j){records=r;json=j;}
    @Cacheable("catalog") public String recordsJson()throws Exception{return json.writeValueAsString(records.selectList(new QueryWrapper<SourceRecord>().orderByAsc("id")));}
    public SourceRecord require(String id,String kind){
        SourceRecord r=records.selectById(id);
        if(r==null||(kind!=null&&!kind.equals(r.kind)))throw ApiException.bad("关联资料不存在或类型不符");
        if("person".equals(kind)&&!"detail".equals(r.recordType))throw ApiException.bad("历史人员尚未匹配，不能直接关联为个人画像");
        return r;
    }
    public String optional(String id,String kind){if(id==null||id.isBlank())return null;require(id,kind);return id;}
}
