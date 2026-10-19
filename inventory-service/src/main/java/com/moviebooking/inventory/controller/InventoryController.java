
package com.moviebooking.inventory.controller;

import com.moviebooking.inventory.entity.ShowInventory;
import com.moviebooking.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @PostMapping("/{showId}/reserve")
    public ResponseEntity<ShowInventory> reserveSeats(
            @PathVariable Long showId,
            @RequestParam Integer seats) {

        ShowInventory inventory =
                inventoryService.reserveSeats(showId, seats);

        return ResponseEntity.ok(inventory);
    }

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<ShowInventory> createInventory(
            @RequestBody ShowInventory inventory) {

        ShowInventory created =
                inventoryService.createInventory(inventory);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ShowInventory>> getAllInventories() {
        return ResponseEntity.ok(
                inventoryService.getAllInventories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowInventory> getInventoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(id));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowInventory>> getInventoriesByMovieId(
            @PathVariable Long movieId) {

        return ResponseEntity.ok(
                inventoryService.getInventoriesByMovieId(movieId));
    }
}
