package fx;

import mj.impl.Obj;

public class TabItem {
    private final Obj obj;
    private final String val;
    public TabItem(Obj obj, String value) {
        this.obj = obj;
        this.val = value;
    }
    public TabItem(Obj obj, int value) {
        this(obj, String.valueOf(value));
    }
    public String getAddress() { return String.valueOf(obj.adr); }
    public String getName() {
        return obj.name;
    }
    public String getType() {
        return obj.type.toString();
    }
    public String getValue() {
        return val;
    }


}
