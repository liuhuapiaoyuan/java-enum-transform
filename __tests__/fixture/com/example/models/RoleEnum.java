import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Optional;

public enum RoleType {
    /** 开发者AAA **/
    DEVELOPER(1, "开发者"),
    // 开发者AAA
    ADMIN((float)0xee,"管理xx"),
    /*
     * 测试管理
    */
    ADMIN(X.s.l.l,"管理"),
    USER(0b1,"用户"),
    /*
     * 测试管理
    */
    USER_X('s',"用户");

    private int value;
    private String name;

    RoleTypeOrigin(int value, String name) {
        this.value = value;
        this.name = name;
    }
}
