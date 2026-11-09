package cn.danxing.platform;

import cn.danxing.platform.mapper.*;
import cn.danxing.platform.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.*;
import io.minio.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class Bootstrap implements ApplicationRunner {
    private static final Logger log=LoggerFactory.getLogger(Bootstrap.class);
    private final AppUserMapper users; private final SourceRecordMapper records; private final JdbcTemplate jdbc;
    private final ObjectMapper json;private final PasswordEncoder encoder;
    private final MinioClient minio;
    @Value("${app.minio.bucket}") String bucket;
    @Value("${app.bootstrap.username}") String username;
    @Value("${app.bootstrap.password}") String password;
    @Value("${app.bootstrap.counselor-p002.username:}") String counselorP002Username;
    @Value("${app.bootstrap.counselor-p002.password:}") String counselorP002Password;
    @Value("${app.bootstrap.counselor-p003.username:}") String counselorP003Username;
    @Value("${app.bootstrap.counselor-p003.password:}") String counselorP003Password;
    @Value("${app.bootstrap.counselor-p001.username:}") String counselorP001Username;
    @Value("${app.bootstrap.counselor-p001.password:}") String counselorP001Password;
    @Value("${app.bootstrap.counselor-p004.username:}") String counselorP004Username;
    @Value("${app.bootstrap.counselor-p004.password:}") String counselorP004Password;
    @Value("${app.bootstrap.counselor-p005.username:}") String counselorP005Username;
    @Value("${app.bootstrap.counselor-p005.password:}") String counselorP005Password;
    @Value("${app.bootstrap.counselor-p006.username:}") String counselorP006Username;
    @Value("${app.bootstrap.counselor-p006.password:}") String counselorP006Password;
    @Value("${app.seed-enabled}") boolean seedEnabled;
    public Bootstrap(AppUserMapper u,SourceRecordMapper r,JdbcTemplate j,ObjectMapper o,PasswordEncoder p,MinioClient m){users=u;records=r;jdbc=j;json=o;encoder=p;minio=m;}
    @Override @Transactional public void run(ApplicationArguments args)throws Exception{
        jdbc.execute("SELECT pg_advisory_xact_lock(271002)");
        if(users.selectCount(new QueryWrapper<AppUser>())==0){
            if(password==null||password.length()<12||password.getBytes(StandardCharsets.UTF_8).length>72)throw new IllegalStateException("ADMIN_PASSWORD must be 12 characters or more, and at most 72 UTF-8 bytes");
            AppUser u=new AppUser();u.id=UUID.randomUUID().toString();u.username=username;u.passwordHash=encoder.encode(password);u.displayName="汤潮";u.role="COUNSELOR_ADMIN";u.createdAt=Instant.now();users.insert(u);
        }
        if(seedEnabled&&jdbc.queryForObject("SELECT count(*) FROM app_meta WHERE key='seed_version'",Integer.class)==0){
            JsonNode seed=json.readTree(new ClassPathResource("seed.json").getInputStream());
            for(JsonNode node:seed.get("records")){
                SourceRecord r=json.treeToValue(node,SourceRecord.class);
                r.staffId=node.path("staff_id").isTextual()?node.get("staff_id").asText():null;
                r.recordType=node.path("record_type").isTextual()?node.get("record_type").asText():null;
                records.insert(r);
            }
            jdbc.update("INSERT INTO app_meta(key,value) VALUES ('seed_version','original-567-v1')");
        }
        ensureCounselorProfiles();
        jdbc.update("""
            UPDATE app_users u SET profile_id=cp.profile_id
            FROM source_records sr JOIN counselor_profiles cp ON cp.profile_id=sr.id
            WHERE u.profile_id IS NULL AND u.display_name=sr.title AND u.role=cp.role_code
              AND (SELECT count(*) FROM app_users same_name
                WHERE same_name.display_name=sr.title AND same_name.role=cp.role_code)=1
            """);
        jdbc.update("""
            UPDATE app_users SET profile_id='P001',
              display_name=CASE WHEN display_name='平台管理员' THEN '汤潮' ELSE display_name END
            WHERE role='COUNSELOR_ADMIN' AND profile_id IS NULL
              AND (SELECT count(*) FROM app_users WHERE role='COUNSELOR_ADMIN')=1
              AND EXISTS (SELECT 1 FROM counselor_profiles WHERE profile_id='P001')
            """);
        List<String> configuredUsernames=List.of(counselorP001Username,counselorP002Username,counselorP003Username,counselorP004Username,counselorP005Username,counselorP006Username)
            .stream().filter(name->!name.isBlank()).toList();
        if(configuredUsernames.stream().distinct().count()!=configuredUsernames.size())
            throw new IllegalStateException("COUNSELOR account usernames must be different");
        ensureInitialCounselor("P001", counselorP001Username, counselorP001Password);
        ensureInitialCounselor("P002", counselorP002Username, counselorP002Password);
        ensureInitialCounselor("P003", counselorP003Username, counselorP003Password);
        ensureInitialCounselor("P004", counselorP004Username, counselorP004Password);
        ensureInitialCounselor("P005", counselorP005Username, counselorP005Password);
        ensureInitialCounselor("P006", counselorP006Username, counselorP006Password);
        seedBundledAvatars();
    }

    /** 将打包在 resources/avatars/<username>.png 的内置头像导入对象存储并关联账号（仅对未设置头像的账号生效）。 */
    private void seedBundledAvatars(){
        try{
            if(!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            for(AppUser u:users.selectList(null)){
                if(u.avatarFileId!=null)continue;
                String name=u.username;
                if(!new ClassPathResource("avatars/"+name+".png").exists()&&new ClassPathResource("avatars/counselor-p001.png").exists()&&"admin".equals(name))name="counselor-p001";
                ClassPathResource res=new ClassPathResource("avatars/"+name+".png");
                if(!res.exists())continue;
                byte[] data;
                try(InputStream in=res.getInputStream()){data=in.readAllBytes();}
                String fileId=UUID.randomUUID().toString();
                String objectKey=u.id+"/"+fileId;
                minio.putObject(PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(new ByteArrayInputStream(data),data.length,-1).contentType("image/png").build());
                jdbc.update("INSERT INTO file_assets(id,owner_id,record_id,original_name,object_key,content_type,size_bytes,created_at) VALUES (?,?,NULL,?,?,?,?,now())",
                    fileId,u.id,name+".png",objectKey,"image/png",(long)data.length);
                jdbc.update("UPDATE app_users SET avatar_file_id=? WHERE id=?",fileId,u.id);
                log.info("已为账号 {} 导入内置头像",u.username);
            }
        }catch(Exception e){
            log.warn("内置头像导入失败（不影响系统启动）：{}",e.toString());
        }
    }

    private void ensureInitialCounselor(String profileId, String accountUsername, String accountPassword){
        boolean hasUsername=!accountUsername.isBlank();
        boolean hasPassword=!accountPassword.isBlank();
        if(!hasUsername&&!hasPassword)return;
        if(hasUsername!=hasPassword)throw new IllegalStateException(
            "COUNSELOR_" + profileId + "_USERNAME and COUNSELOR_" + profileId + "_PASSWORD must both be configured");
        if(!accountUsername.matches("[A-Za-z0-9_.-]{3,60}"))
            throw new IllegalStateException("COUNSELOR_" + profileId + "_USERNAME must be 3–60 letters, digits, or ._-");
        if(accountPassword.length()<12||accountPassword.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalStateException("COUNSELOR_" + profileId + "_PASSWORD must be at least 12 characters and at most 72 UTF-8 bytes");

        Integer profileCount=jdbc.queryForObject("""
            SELECT count(*) FROM counselor_profiles
            WHERE profile_id=? AND role_code='COUNSELOR'
            """,Integer.class,profileId);
        if(profileCount==null||profileCount!=1)throw new IllegalStateException(
            "Cannot create the counselor account: profile " + profileId + " is missing; enable and import seed data first");

        AppUser existing=users.selectOne(new QueryWrapper<AppUser>().eq("username",accountUsername));
        if(existing!=null){
            if("COUNSELOR".equals(existing.role)&&profileId.equals(existing.profileId))return;
            if("COUNSELOR".equals(existing.role)&&List.of("P001","P002","P003","P004","P005","P006").contains(existing.profileId)){
                Integer targetAccountCount=jdbc.queryForObject(
                    "SELECT count(*) FROM app_users WHERE profile_id=? AND role='COUNSELOR'",Integer.class,profileId);
                if(targetAccountCount!=null&&targetAccountCount>0)throw new IllegalStateException(
                    "Cannot link " + accountUsername + " to profile " + profileId + ": that profile is already linked to another account");
                jdbc.update("""
                    UPDATE app_users SET profile_id=?, display_name=(
                      SELECT title FROM source_records WHERE id=?
                    ) WHERE id=? AND profile_id=? AND role='COUNSELOR'
                    """,profileId,profileId,existing.id,existing.profileId);
                return;
            }
            throw new IllegalStateException("COUNSELOR_" + profileId + "_USERNAME is already used by another account");
        }
        Integer accountCount=jdbc.queryForObject(
            "SELECT count(*) FROM app_users WHERE profile_id=? AND role='COUNSELOR'",Integer.class,profileId);
        if(accountCount!=null&&accountCount>0)throw new IllegalStateException(
            "Cannot create the counselor account: profile " + profileId + " is already linked to another account");

        String displayName=jdbc.queryForObject("SELECT title FROM source_records WHERE id=?",String.class,profileId);
        AppUser user=new AppUser();
        user.id=UUID.randomUUID().toString();
        user.username=accountUsername;
        user.passwordHash=encoder.encode(accountPassword);
        user.displayName=displayName;
        user.role="COUNSELOR";
        user.profileId=profileId;
        user.createdAt=Instant.now();
        users.insert(user);
    }

    private void ensureCounselorProfiles(){
        jdbc.update("""
            INSERT INTO counselor_profiles(profile_id,role_code,manager_profile_id)
            SELECT id,
              CASE WHEN id='P001' THEN 'COUNSELOR_ADMIN' ELSE 'COUNSELOR' END,
              CASE WHEN id='P001' THEN NULL ELSE 'P001' END
            FROM source_records
            WHERE kind='person' AND record_type='detail' AND id IN ('P001','P002','P003','P004','P005','P006')
            ON CONFLICT(profile_id,role_code) DO NOTHING
            """);
        jdbc.update("""
            INSERT INTO counselor_profiles(profile_id,role_code,manager_profile_id)
            SELECT id,'COUNSELOR',NULL
            FROM source_records
            WHERE kind='person' AND record_type='detail' AND id='P001'
            ON CONFLICT(profile_id,role_code) DO NOTHING
            """);
    }
}
