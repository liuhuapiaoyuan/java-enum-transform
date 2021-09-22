import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

public enum RoleType {
    DEVELOPER(1, "开发者"),
    ADMIN((float)2.2,"管理xx"),
    ADMIN((int)3,"管理"),
    USER((short)4,"用户");

    private int value;
    private String name;

    RoleTypeOrigin(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
