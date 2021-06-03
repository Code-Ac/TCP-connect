import java.util.ArrayList;
import java.util.Arrays;

public class DataPackage extends ArrayList<Object> {
    private String cid = null;

    public DataPackage(String id, Object... objects) {
        this.add(id);
        this.addAll(1, Arrays.asList(objects));
    }

    public String getId() {
        if (this.get(0) instanceof String) {
            return (String) this.get(0);
        } else {
            throw new IllegalArgumentException("Package ID must be String");
        }
    }

    public String getClientId() {
        return this.cid;
    }

    public void setClientId(String id) {
        this.cid = id;
    }
}
