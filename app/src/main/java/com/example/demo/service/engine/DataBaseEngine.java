package com.example.demo.service.engine;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


public class DataBaseEngine {
    private Map<String, Table> tables;
    private String dbPath;

    public DataBaseEngine(String dbPath) {
        this.tables = new HashMap<>();
        this.dbPath = dbPath;
    }

    public void createTable(String tableName) {
        if (!tables.containsKey(tableName)) {
            tables.put(tableName, new Table(tableName));
            System.out.println("Tabla '" + tableName + "' creada exitosamente.");
        } else {
            System.out.println("La tabla '" + tableName + "' ya existe.");
        }
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public void dropTable(String tableName) {
        if (tables.remove(tableName) != null) {
            System.out.println("Tabla '" + tableName + "' eliminada.");
        } else {
            System.out.println("Tabla '" + tableName + "' no existe.");
        }
    }

    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dbPath))) {
            oos.writeObject(this);
            System.out.println("Base de datos guardada en: " + dbPath);
        } catch (IOException e) {
            System.err.println("Error al guardar: " + e.getMessage());
        }
    }

    public static DataBaseEngine load(String dbPath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dbPath))) {
            System.out.println("Base de datos cargada desde: " + dbPath);
            return (DataBaseEngine) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Creando nueva base de datos...");
            return new DataBaseEngine(dbPath);
        }
    }

    public void showTables() {
        if (tables.isEmpty()) {
            System.out.println("No hay tablas en la base de datos.");
        } else {
            System.out.println("Tablas:");
            for (String tableName : tables.keySet()) {
                System.out.println("  - " + tableName);
            }
        }
    }

    // Nuevo: devolver nombres de tablas (API)
    public List<String> getTableNames() {
        return new ArrayList<>(tables.keySet());
    }
}
