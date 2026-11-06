package cn.danxing.platform;

import cn.danxing.platform.mapper.FileAssetMapper;
import cn.danxing.platform.model.FileAsset;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FileService {
    private static final Logger LOGGER=Logger.getLogger(FileService.class.getName());
    private final MinioClient minio;private final FileAssetMapper files;private final CurrentUser user;private final CatalogService catalog;private final AuditService audit;private final TransactionTemplate tx;private final JdbcTemplate jdbc;
    @Value("${app.minio.bucket}") String bucket;
    private static final Set<String> ALLOWED=Set.of("pdf","doc","docx","xls","xlsx","ppt","pptx","txt","csv","png","jpg","jpeg","zip");
    public FileService(MinioClient m,FileAssetMapper f,CurrentUser u,CatalogService c,AuditService a,TransactionTemplate t,JdbcTemplate j){minio=m;files=f;user=u;catalog=c;audit=a;tx=t;jdbc=j;}
    public List<FileAsset> list(){return files.selectList(new QueryWrapper<FileAsset>().eq("owner_id",user.id()).orderByDesc("created_at"));}
    public FileAsset owned(String id){FileAsset f=files.selectOne(new QueryWrapper<FileAsset>().eq("id",id).eq("owner_id",user.id()));if(f==null)throw ApiException.missing();return f;}
    public FileAsset visible(String id){FileAsset f=files.selectById(id);if(f==null)throw ApiException.missing();return f;}
    public synchronized void ensureBucket()throws Exception{
        if(!minio.bucketExists(BucketExistsArgs.builder().bucket(bucket).build()))minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
    public FileAsset upload(MultipartFile upload,String recordId)throws Exception{
        if(upload.isEmpty()||upload.getSize()>20L*1024*1024)throw ApiException.bad("请选择 20 MB 以内的非空文件");
        String name=Objects.toString(upload.getOriginalFilename(),"attachment").replaceAll("[\\\\/\\p{Cntrl}]","_");
        if(name.length()>200)throw ApiException.bad("文件名过长");
        String ext=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);
        if(!ALLOWED.contains(ext))throw ApiException.bad("支持 Office、PDF、图片、TXT、CSV 和 ZIP 文件");
        FileAsset f=new FileAsset();f.id=UUID.randomUUID().toString();f.ownerId=user.id();f.recordId=catalog.optional(recordId,null);f.originalName=name;f.objectKey=f.ownerId+"/"+f.id;f.contentType="application/octet-stream";f.sizeBytes=upload.getSize();f.createdAt=Instant.now();
        ensureBucket();
        try(InputStream stream=upload.getInputStream()){minio.putObject(PutObjectArgs.builder().bucket(bucket).object(f.objectKey).stream(stream,f.sizeBytes,-1).contentType(f.contentType).build());}
        try {tx.executeWithoutResult(s->{files.insert(f);audit.write("上传附件",f.id,f.originalName);});}
        catch(RuntimeException failure){try{minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(f.objectKey).build());}catch(Exception cleanup){failure.addSuppressed(cleanup);}throw failure;}
        return f;
    }
    public InputStream download(FileAsset f)throws Exception{return minio.getObject(GetObjectArgs.builder().bucket(bucket).object(f.objectKey).build());}
    public void deleteAvatarIfUnreferenced(String id){
        try{
            Integer references=jdbc.queryForObject("SELECT count(*) FROM app_users WHERE avatar_file_id=?",Integer.class,id);
            if(references==null||references>0)return;
            FileAsset f=files.selectById(id);
            if(f==null)return;
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(f.objectKey).build());
            tx.executeWithoutResult(s->{
                Integer remaining=jdbc.queryForObject("SELECT count(*) FROM app_users WHERE avatar_file_id=?",Integer.class,id);
                if(remaining==null||remaining==0){
                    files.deleteById(id);
                    audit.write("删除未使用头像",id,f.originalName);
                }
            });
        }catch(Exception failure){
            LOGGER.log(Level.WARNING,"Could not remove replaced avatar asset " + id,failure);
        }
    }
    // Remove DB metadata only after object deletion succeeds; repeated object deletion is safe.
    public void delete(String id)throws Exception{
        FileAsset f=owned(id);minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(f.objectKey).build());
        tx.executeWithoutResult(s->{files.deleteById(id);audit.write("删除附件",id,f.originalName);});
    }
}
