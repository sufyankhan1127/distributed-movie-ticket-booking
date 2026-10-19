
package com.moviebooking.inventory.service;

import com.moviebooking.inventory.entity.ShowInventory;
import com.moviebooking.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public ShowInventory createInventory(ShowInventory inventory) {
        if (inventory.getTotalSeats() == null
                || inventory.getTotalSeats() <= 0) {
            throw new IllegalArgumentException(
                    "Total seats must be greater than zero");
        }

        if (inventory.getTicketPrice() == null
                || inventory.getTicketPrice() <= 0) {
            throw new IllegalArgumentException(
                    "Ticket price must be greater than zero");
        }

        inventory.setAvailableSeats(inventory.getTotalSeats());

        return inventoryRepository.save(inventory);
    }

    public List<ShowInventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    public ShowInventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Show inventory not found with ID: " + id));
    }

    public List<ShowInventory> getInventoriesByMovieId(Long movieId) {
        return inventoryRepository.findByMovieId(movieId);
    }

    @Transactional
    public ShowInventory reserveSeats(Long showId, Integer seats) {
        if (seats == null || seats <= 0) {
            throw new IllegalArgumentException(
                    "Number of seats must be greater than zero");
        }

        int updatedRows = inventoryRepository.reserveSeats(showId, seats);

        if (updatedRows == 0) {
            throw new IllegalArgumentException(
                    "Show not found or insufficient seats available");
        }

        return getInventoryById(showId);
    }
}
