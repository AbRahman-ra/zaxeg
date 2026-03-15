package sa.abrahman.zaxeg.core.validator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValueValidator {
    public boolean assertEqualIgnorecase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}
