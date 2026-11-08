package cn.danxing.platform;
import java.time.*;
import java.util.Map;
import java.util.concurrent.*;
public class LoginGuard {
    private static final int MAX_FAILURES=5;
    private static final Duration LOCK_DURATION=Duration.ofMinutes(10);
    private record Failure(int count,Instant lockedUntil){}
    private final Map<String,Failure> failures=new ConcurrentHashMap<>();
    private String key(String username,String ip){return username+"|"+ip;}
    public boolean blocked(String username,String ip){
        Failure f=failures.get(key(username,ip));
        return f!=null&&f.lockedUntil().isAfter(Instant.now());
    }
    public void onFailure(String username,String ip){
        if(failures.size()>5000)failures.clear();
        failures.merge(key(username,ip),new Failure(1,Instant.EPOCH),
            (prev,ignored)->{
                int count=prev.count()+1;
                return new Failure(count,count>=MAX_FAILURES?Instant.now().plus(LOCK_DURATION):Instant.EPOCH);
            });
    }
    public void onSuccess(String username,String ip){failures.remove(key(username,ip));}
}
