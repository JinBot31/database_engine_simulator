package com.example.demo.service.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Record implements Serializable {
    private Map<String, Object> fields;

    public Record() {
        this.fields = new HashMap<>();
    }

    public void setField(String name, Object value) {
        fields.put(name, value);
    }

    public Object getField(String name) {
        return fields.get(name);
    }

    public Map<String, Object> getAllFields() {
        return new HashMap<>(fields);
    }

    @Override
    public String toString() {
        return fields.toString();
    }
}
