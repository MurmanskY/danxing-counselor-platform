package cn.danxing.platform;
import cn.danxing.platform.mapper.FileAssetMapper;
import cn.danxing.platform.model.FileAsset;
import io.minio.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {
    MinioClient minio;FileAssetMapper mapper;CurrentUser user;CatalogService catalog;AuditService audit;TransactionTemplate tx;JdbcTemplate jdbc;FileService service;
    @BeforeEach void setup(){minio=mock(MinioClient.class);mapper=mock(FileAssetMapper.class);user=mock(CurrentUser.class);catalog=mock(CatalogService.class);audit=mock(AuditService.class);tx=mock(TransactionTemplate.class);jdbc=mock(JdbcTemplate.class);when(user.id()).thenReturn("owner-a");service=new FileService(minio,mapper,user,catalog,audit,tx,jdbc);service.bucket="test-private";}
    @Test void otherOwnersFileCannotBeResolved(){assertEquals(404,assertThrows(ApiException.class,()->service.owned("foreign")).status);verifyNoInteractions(minio);}
    @Test void otherOwnersFileCannotBeDeleted(){assertThrows(ApiException.class,()->service.delete("foreign"));verifyNoInteractions(minio);}
    @Test void referencedAvatarIsNotDeleted(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("in-use"))).thenReturn(1);
        service.deleteAvatarIfUnreferenced("in-use");
        verifyNoInteractions(minio,mapper);
    }
    @Test void emptyFilesAreRejected(){assertThrows(ApiException.class,()->service.upload(new MockMultipartFile("file","empty.txt","text/plain",new byte[0]),null));verifyNoInteractions(minio);}
    @Test void executableExtensionIsRejected(){assertThrows(ApiException.class,()->service.upload(new MockMultipartFile("file","test.exe","application/octet-stream",new byte[]{1}),null));verifyNoInteractions(minio);}
    @Test void failedMetadataWriteRemovesUploadedObject()throws Exception{
        when(minio.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        doThrow(new RuntimeException("database write failed")).when(tx).executeWithoutResult(any());
        assertThrows(RuntimeException.class,()->service.upload(new MockMultipartFile("file","test.txt","text/plain","test".getBytes()),null));
        verify(minio).putObject(any(PutObjectArgs.class));verify(minio).removeObject(any(RemoveObjectArgs.class));
    }
    @Test void failedObjectDeleteKeepsMetadata()throws Exception{
        FileAsset f=new FileAsset();f.id="own";f.objectKey="owner-a/own";when(mapper.selectOne(any())).thenReturn(f);
        doThrow(new java.io.IOException("storage unavailable")).when(minio).removeObject(any(RemoveObjectArgs.class));
        assertThrows(java.io.IOException.class,()->service.delete("own"));verifyNoInteractions(tx);
    }
}
