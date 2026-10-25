package com.hearth.care.api;

import com.hearth.care.application.GroceryService;
import com.hearth.care.application.GroceryService.CreateGroceryRequest;
import com.hearth.care.application.GroceryService.GroceryItemView;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HEAR-462: REST controller for the shared family shopping / grocery list.
 */
@RestController
@RequestMapping("/api/v1/care/groceries")
public class GroceryController {

    private final GroceryService groceryService;

    public GroceryController(GroceryService groceryService) {
        this.groceryService = groceryService;
    }

    @GetMapping
    public List<GroceryItemView> list() {
        return groceryService.list();
    }

    @PostMapping
    public ResponseEntity<GroceryItemView> add(@RequestBody CreateGroceryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groceryService.add(request));
    }

    @PatchMapping("/{id}/toggle")
    public GroceryItemView toggle(@PathVariable UUID id) {
        return groceryService.toggleChecked(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        groceryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/checked")
    public ResponseEntity<Void> clearChecked() {
        groceryService.clearChecked();
        return ResponseEntity.noContent().build();
    }
}
