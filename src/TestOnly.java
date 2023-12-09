import java.util.HashMap;

public class TestOnly {

    static HashMap hm = new HashMap();

    public static void main(String[] args) {
        hm.put("111", 111);
        hm.remove("111");
    }
}
