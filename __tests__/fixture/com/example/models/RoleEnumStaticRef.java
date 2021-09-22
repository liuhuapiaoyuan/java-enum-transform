import static com.example.consts.UserType;

public enum RoleStaticRefType {
    DEVELOPER(UserType.DEFAULT_1, "开发者"),
    ADMIN(UserType.DEFAULT_2, "管理");
    USER(3, "用户");
}
