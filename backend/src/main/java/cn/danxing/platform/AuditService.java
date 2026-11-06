package cn.danxing.platform;
import cn.danxing.platform.mapper.AuditLogMapper;
import cn.danxing.platform.model.AuditLog;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service
public class AuditService {
    private final AuditLogMapper logs;private final CurrentUser user;
    public AuditService(AuditLogMapper l,CurrentUser u){logs=l;user=u;}
    public void write(String action,String target,String detail){AuditLog l=new AuditLog();l.actorId=user.id();l.action=action;l.targetId=target;l.detail=detail;l.createdAt=Instant.now();logs.insert(l);}
}
