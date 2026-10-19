
package com.moviebooking.inventory.repository;

import com.moviebooking.inventory.entity.ShowInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository
        extends JpaRepository<ShowInventory, Long> {

    List<ShowInventory> findByMovieId(Long movieId);

    @Modifying
    @Query("""
        UPDATE ShowInventory s
        SET s.availableSeats = s.availableSeats - :seats
        WHERE s.id = :showId
          AND s.availableSeats >= :seats
        """)
    int reserveSeats(
            @Param("showId") Long showId,
            @Param("seats") Integer seats);
}
