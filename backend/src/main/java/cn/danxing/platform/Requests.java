package cn.danxing.platform;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class Requests {
    private Requests(){}
    public record PlanInput(
        @NotBlank(message="请填写计划标题") @Size(max=120) String title,
        @NotBlank @Size(max=60) String domain, LocalDate deadline,
        @Size(max=2000) String description, String personId, String opportunityId,
        @NotEmpty(message="至少添加一项任务") @Size(max=12) List<@NotBlank @Size(max=180) String> tasks){}
    public record PlanEdit(@NotBlank @Size(max=120) String title,LocalDate deadline,@Size(max=2000) String description){}
    public record Done(@NotNull Boolean done){}
    public record FavoriteInput(@NotNull Boolean active){}
    public record CalendarInput(@NotBlank @Size(max=150) String title,@NotNull LocalDate startDate,@NotNull LocalDate endDate,
        @Pattern(regexp="个人准备|申报安排（待核验）") @NotNull String category,@Size(max=1000) String note,String recordId){}
    public record ReviewInput(@Size(max=200) String keywords,@Size(max=1000) String philosophy,
        @Pattern(regexp="待核验|已核验|需修正") @NotNull String reviewStatus,@Size(max=1000) String note){}
    public record UserInput(@Pattern(regexp="[A-Za-z0-9_.-]{3,60}",message="账号为 3–60 位字母、数字或 ._- ") @NotNull String username,
        @NotBlank @Size(min=12,max=72,message="密码长度为 12–72 个字符") String password,
        @NotBlank @Size(max=80) String displayName,@NotBlank @Size(max=40) String role,
        @NotBlank @Size(max=20) String profileId){}
}
