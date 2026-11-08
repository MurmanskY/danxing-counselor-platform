package cn.danxing.platform;
import cn.danxing.platform.mapper.*;
import cn.danxing.platform.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.List;

class PlanServiceTest {
    PlanMapper plans;TaskMapper tasks;CurrentUser user;CatalogService catalog;AuditService audit;PlanService service;
    @BeforeEach void setup(){plans=mock(PlanMapper.class);tasks=mock(TaskMapper.class);user=mock(CurrentUser.class);catalog=mock(CatalogService.class);audit=mock(AuditService.class);service=new PlanService(plans,tasks,user,catalog,audit);when(user.id()).thenReturn("owner-a");}
    @Test void cannotEditAnotherOwnersPlan(){when(plans.lock("foreign","owner-a")).thenReturn(null);assertEquals(404,assertThrows(ApiException.class,()->service.edit("foreign",new Requests.PlanEdit("changed",LocalDate.now(),""))).status);verify(plans,never()).update(any(),any());}
    @Test void cannotDeleteAnotherOwnersPlan(){assertThrows(ApiException.class,()->service.delete("foreign"));verify(plans,never()).deleteById(anyString());}
    @Test void cannotToggleAnotherOwnersTask(){Task t=new Task();t.id="task";t.planId="foreign";when(tasks.selectById("task")).thenReturn(t);assertThrows(ApiException.class,()->service.toggle("task",true));verify(tasks,never()).updateById(any(Task.class));}
    @Test void completingLastTaskRefreshesPlanStatusAtomically(){Task t=new Task();t.id="task";t.planId="own";Plan p=new Plan();p.id="own";p.ownerId="owner-a";p.status="进行中";when(tasks.selectById("task")).thenReturn(t);when(plans.lock("own","owner-a")).thenReturn(p);service.toggle("task",true);assertTrue(t.done);verify(plans).update(isNull(),argThat(w->w.getSqlSet()!=null&&w.getSqlSet().contains("CASE WHEN")));verify(plans,never()).updateById(any(Plan.class));}
    @Test void validatesRelationshipsBeforeInsert(){when(catalog.optional("legacy","person")).thenThrow(ApiException.bad("unmatched"));var in=new Requests.PlanInput("plan","domain",null,"","legacy",null,List.of("task"));assertThrows(ApiException.class,()->service.create(in));verify(plans,never()).insert(any(Plan.class));}
    @Test void csvNeutralizesFormulaAndQuotes(){assertEquals("\"'=1+1\"",PlanService.cell("=1+1"));assertEquals("\"'  +SUM(1,2)\nnext\"",PlanService.cell("  +SUM(1,2)\nnext"));assertEquals("\"a\"\"b\"",PlanService.cell("a\"b"));}
}
