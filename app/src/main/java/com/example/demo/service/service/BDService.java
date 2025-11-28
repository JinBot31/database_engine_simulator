package com.example.demo.service.service;

import com.example.demo.service.engine.DataBaseEngine;
import com.example.demo.service.engine.Record;
import com.example.demo.service.engine.Table;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BDService {

    private DataBaseEngine engine;
    private final String DB_PATH = "mibasedatos.db";

    public BDService() {
        this.engine = DataBaseEngine.load(DB_PATH);
        // register shutdown hook to save DB on exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.engine != null) this.engine.save();
        }));
    }

    public List<String> getTables() {
        return engine.getTableNames();
    }

    public void createTable(String name) {
        engine.createTable(name);
    }

    public List<Map<String, Object>> getAllRecords(String tableName) {
        Table t = engine.getTable(tableName);
        if (t == null) return null;
        List<Record> recs = t.selectAll();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Record r : recs) out.add(r.getAllFields());
        return out;
    }

    public Map<String, Object> getRecord(String tableName, int id) {
        Table t = engine.getTable(tableName);
        if (t == null) return null;
        Record r = t.select(id);
        return r == null ? null : r.getAllFields();
    }

    public Integer insertRecord(String tableName, Map<String, Object> data) {
        Table t = engine.getTable(tableName);
        if (t == null) return null;
        Record r = new Record();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if ("id".equals(e.getKey())) continue;
            r.setField(e.getKey(), e.getValue());
        }
        return t.insert(r);
    }

    public boolean updateRecord(String tableName, int id, Map<String, Object> data) {
        Table t = engine.getTable(tableName);
        if (t == null) return false;
        Record existing = t.select(id);
        if (existing == null) return false;
        Record nr = new Record();
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if ("id".equals(e.getKey())) continue;
            nr.setField(e.getKey(), e.getValue());
        }
        t.update(id, nr);
        return true;
    }

    public boolean deleteRecord(String tableName, int id) {
        Table t = engine.getTable(tableName);
        if (t == null) return false;
        Record existing = t.select(id);
        if (existing == null) return false;
        t.delete(id);
        return true;
    }

    public boolean createIndex(String tableName, String field) {
        Table t = engine.getTable(tableName);
        if (t == null) return false;
        t.createIndex(field);
        return true;
    }

    public boolean deleteTable(String tableName) {
        engine.dropTable(tableName);
        return true;
    }

    public List<Map<String, Object>> selectByIndex(String tableName, String field, String value) {
        Table t = engine.getTable(tableName);
        if (t == null) return null;
        Comparable comp = parseComparable(value);
        List<Record> recs = t.selectByIndex(field, comp);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Record r : recs) out.add(r.getAllFields());
        return out;
    }

    private Comparable<?> parseComparable(String v) {
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            // not an integer, return original string
            return v;
        }
    }
}

