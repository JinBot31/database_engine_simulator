package com.example.demo.service.engine;

import java.io.Serializable;
import java.util.*;

// Tabla de la base de datos
public class Table implements Serializable {
    private String name;
    private AVLTree<Integer, Record> primaryIndex;
    @SuppressWarnings("rawtypes")
    private Map<String, AVLTree> secondaryIndexes;
    private int nextId;

    public Table(String name) {
        this.name = name;
        this.primaryIndex = new AVLTree<>();
        this.secondaryIndexes = new HashMap<>();
        this.nextId = 1;
    }

    @SuppressWarnings("unchecked")
    public int insert(Record record) {
        int id = nextId++;
        record.setField("id", id);
        primaryIndex.insert(id, record);

        // Actualizar índices secundarios
        for (Map.Entry<String, AVLTree> entry : secondaryIndexes.entrySet()) {
            String fieldName = entry.getKey();
            Object value = record.getField(fieldName);
            if (value instanceof Comparable) {
                AVLTree tree = entry.getValue();
                List<Integer> ids = (List<Integer>) tree.search((Comparable) value);
                if (ids == null) {
                    ids = new ArrayList<>();
                }
                ids.add(id);
                tree.insert((Comparable) value, ids);
            }
        }

        return id;
    }

    public Record select(int id) {
        return primaryIndex.search(id);
    }

    public List<Record> selectAll() {
        List<Record> results = new ArrayList<>();
        primaryIndex.inOrderTraversal(results);
        return results;
    }

    @SuppressWarnings("unchecked")
    public void update(int id, Record newRecord) {
        Record oldRecord = primaryIndex.search(id);
        if (oldRecord != null) {
            newRecord.setField("id", id);
            primaryIndex.insert(id, newRecord);

            // Actualizar índices secundarios
            for (String fieldName : secondaryIndexes.keySet()) {
                Object oldValue = oldRecord.getField(fieldName);
                Object newValue = newRecord.getField(fieldName);

                if (oldValue instanceof Comparable && newValue instanceof Comparable) {
                    AVLTree index = secondaryIndexes.get(fieldName);

                    // Remover de índice antiguo
                    List<Integer> oldIds = (List<Integer>) index.search((Comparable) oldValue);
                    if (oldIds != null) {
                        oldIds.remove(Integer.valueOf(id));
                        if (oldIds.isEmpty()) {
                            index.delete((Comparable) oldValue);
                        }
                    }

                    // Agregar a nuevo índice
                    List<Integer> newIds = (List<Integer>) index.search((Comparable) newValue);
                    if (newIds == null) {
                        newIds = new ArrayList<>();
                    }
                    newIds.add(id);
                    index.insert((Comparable) newValue, newIds);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void delete(int id) {
        Record record = primaryIndex.search(id);
        if (record != null) {
            primaryIndex.delete(id);

            // Actualizar índices secundarios
            for (Map.Entry<String, AVLTree> entry : secondaryIndexes.entrySet()) {
                String fieldName = entry.getKey();
                Object value = record.getField(fieldName);
                if (value instanceof Comparable) {
                    AVLTree tree = entry.getValue();
                    List<Integer> ids = (List<Integer>) tree.search((Comparable) value);
                    if (ids != null) {
                        ids.remove(Integer.valueOf(id));
                        if (ids.isEmpty()) {
                            tree.delete((Comparable) value);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void createIndex(String fieldName) {
        if (!secondaryIndexes.containsKey(fieldName)) {
            AVLTree index = new AVLTree();

            // Construir índice con datos existentes
            List<Record> allRecords = selectAll();
            for (Record record : allRecords) {
                Object value = record.getField(fieldName);
                if (value instanceof Comparable) {
                    Comparable key = (Comparable) value;
                    List<Integer> ids = (List<Integer>) index.search(key);
                    if (ids == null) {
                        ids = new ArrayList<>();
                    }
                    ids.add((Integer) record.getField("id"));
                    index.insert(key, ids);
                }
            }

            secondaryIndexes.put(fieldName, index);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Record> selectByIndex(String fieldName, Comparable value) {
        List<Record> results = new ArrayList<>();
        AVLTree index = secondaryIndexes.get(fieldName);

        if (index != null) {
            List<Integer> ids = (List<Integer>) index.search(value);
            if (ids != null) {
                for (Integer id : ids) {
                    Record record = primaryIndex.search(id);
                    if (record != null) {
                        results.add(record);
                    }
                }
            }
        }

        return results;
    }

    public String getName() {
        return name;
    }

}
