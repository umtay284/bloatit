package test;

import java.util.HashMap;

public class Parameters extends HashMap<String, String> {
    private static final long serialVersionUID = 1L;

    public Parameters() {
        super();
    }

    public Parameters(String name, String value) {
        super();
        super.put(name, value);
    }

    public Parameters add(String name, String value) {
        super.put(name, value);
        return this;
    }

}