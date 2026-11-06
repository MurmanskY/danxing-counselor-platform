package cn.danxing.platform.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.danxing.platform.model.Plan;
public interface PlanMapper extends BaseMapper<Plan> {
    @org.apache.ibatis.annotations.Select("SELECT * FROM plans WHERE id=#{id} AND owner_id=#{owner} FOR UPDATE")
    Plan lock(@org.apache.ibatis.annotations.Param("id") String id,@org.apache.ibatis.annotations.Param("owner") String owner);
}
