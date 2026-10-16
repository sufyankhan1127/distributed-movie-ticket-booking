
package com.moviebooking.inventory.repository;

import com.moviebooking.inventory.entity.ShowInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository
        extends JpaRepository<ShowInventory, Long> {

    List<ShowInventory> findByMovieId(Long movieId);

}
