package cn.danxing.platform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.*;
import java.sql.*;
import java.util.Map;

@MappedTypes(Map.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonMapHandler extends BaseTypeHandler<Map<String,Object>> {
    private static final ObjectMapper JSON = new ObjectMapper().findAndRegisterModules();
    @Override public void setNonNullParameter(PreparedStatement ps,int i,Map<String,Object> v,JdbcType t) throws SQLException {
        try { ps.setObject(i,JSON.writeValueAsString(v),Types.OTHER); } catch(Exception e) { throw new SQLException("JSON serialization failed",e); }
    }
    private Map<String,Object> read(String s) throws SQLException {
        try { return s==null ? Map.of() : JSON.readValue(s,new TypeReference<>(){}); } catch(Exception e){throw new SQLException("Invalid JSON",e);}
    }
    @Override public Map<String,Object> getNullableResult(ResultSet r,String c)throws SQLException{return read(r.getString(c));}
    @Override public Map<String,Object> getNullableResult(ResultSet r,int c)throws SQLException{return read(r.getString(c));}
    @Override public Map<String,Object> getNullableResult(CallableStatement r,int c)throws SQLException{return read(r.getString(c));}
}
