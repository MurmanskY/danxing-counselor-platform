package cn.danxing.platform;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService files;
    public FileController(FileService f){files=f;}
    @GetMapping public Object list(){return files.list();}
    @PostMapping public Object upload(@RequestParam MultipartFile file,@RequestParam(required=false) String recordId)throws Exception{return files.upload(file,recordId);}
    @GetMapping("/{id}/download") public ResponseEntity<InputStreamResource> download(@PathVariable String id)throws Exception{
        var f=files.owned(id);return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(f.sizeBytes)
            .header("X-Content-Type-Options","nosniff").header("Content-Disposition",ContentDisposition.attachment().filename(f.originalName,StandardCharsets.UTF_8).build().toString()).body(new InputStreamResource(files.download(f)));
    }
    @GetMapping("/{id}/avatar") public ResponseEntity<InputStreamResource> avatar(@PathVariable String id)throws Exception{
        var f=files.visible(id);MediaType type=f.originalName!=null&&f.originalName.toLowerCase(java.util.Locale.ROOT).endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(type).contentLength(f.sizeBytes).cacheControl(CacheControl.maxAge(1,java.util.concurrent.TimeUnit.HOURS).cachePublic())
            .header("X-Content-Type-Options","nosniff").body(new InputStreamResource(files.download(f)));
    }
    @DeleteMapping("/{id}") public void delete(@PathVariable String id)throws Exception{files.delete(id);}
}
