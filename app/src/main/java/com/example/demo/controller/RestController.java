package com.example.demo.controller;

import com.example.demo.service.engine.Record;
import com.example.demo.service.service.BDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RestController {

	@Autowired
	private BDService bdService;

	@GetMapping("/tables")
	public List<String> listTables() {
		return bdService.getTables();
	}

	@PostMapping("/tables")
	public ResponseEntity<?> createTable(@RequestBody Map<String, String> body) {
		String name = body.get("name");
		if (name == null || name.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "table name required"));
		}

		bdService.createTable(name);
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@DeleteMapping("/tables/{table}")
	public ResponseEntity<?> deleteTable(@PathVariable String table) {
		bdService.deleteTable(table);
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@GetMapping("/tables/{table}/records")
	public ResponseEntity<?> getAllRecords(@PathVariable String table) {
		List<Map<String, Object>> records = bdService.getAllRecords(table);
		if (records == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(records);
	}

	@GetMapping("/tables/{table}/records/{id}")
	public ResponseEntity<?> getRecord(@PathVariable String table, @PathVariable int id) {
		Map<String, Object> rec = bdService.getRecord(table, id);
		if (rec == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(rec);
	}

	@PostMapping("/tables/{table}/records")
	public ResponseEntity<?> insertRecord(@PathVariable String table, @RequestBody Map<String, Object> body) {
		Integer id = bdService.insertRecord(table, body);
		if (id == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(Map.of("id", id));
	}

	@PutMapping("/tables/{table}/records/{id}")
	public ResponseEntity<?> updateRecord(@PathVariable String table, @PathVariable int id, @RequestBody Map<String, Object> body) {
		boolean ok = bdService.updateRecord(table, id, body);
		if (!ok) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@DeleteMapping("/tables/{table}/records/{id}")
	public ResponseEntity<?> deleteRecord(@PathVariable String table, @PathVariable int id) {
		boolean ok = bdService.deleteRecord(table, id);
		if (!ok) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@PostMapping("/tables/{table}/indexes")
	public ResponseEntity<?> createIndex(@PathVariable String table, @RequestBody Map<String, String> body) {
		String field = body.get("field");
		if (field == null || field.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "field required"));
		}
		boolean ok = bdService.createIndex(table, field);
		if (!ok) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(Map.of("ok", true));
	}

	@GetMapping("/tables/{table}/indexes/{field}")
	public ResponseEntity<?> queryByIndex(@PathVariable String table, @PathVariable String field, @RequestParam("value") String value) {
		List<Map<String, Object>> results = bdService.selectByIndex(table, field, value);
		if (results == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(results);
	}

}
