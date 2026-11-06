package cn.danxing.platform;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class PlanController {
    private final PlanService plans;
    public PlanController(PlanService p){plans=p;}
    @GetMapping("/plans") public Object list(){return plans.list();}
    @PostMapping("/plans") public Object create(@Valid @RequestBody Requests.PlanInput in){return plans.create(in);}
    @PatchMapping("/plans/{id}") public void edit(@PathVariable String id,@Valid @RequestBody Requests.PlanEdit in){plans.edit(id,in);}
    @DeleteMapping("/plans/{id}") public void delete(@PathVariable String id){plans.delete(id);}
    @PatchMapping("/tasks/{id}") public void toggle(@PathVariable String id,@Valid @RequestBody Requests.Done in){plans.toggle(id,in.done());}
    @GetMapping("/plans/export") public ResponseEntity<String> export(){return ResponseEntity.ok().header("Content-Disposition","attachment; filename=danxing-plans.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(plans.csv());}
}
