
package com.moviebooking.booking.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class InventoryClient {

    private final RestTemplate restTemplate;

    public InventoryClient() {
        this.restTemplate = new RestTemplate();
    }

    public InventoryDetails getInventory(Long showId) {
        try {
            Map<?, ?> response = restTemplate.getForObject(
                    "http://localhost:8083/api/inventory/" + showId,
                    Map.class);

            if (response == null) {
                throw new IllegalStateException(
                        "Inventory Service returned an empty response");
            }

            Object seats = response.get("availableSeats");
            Object price = response.get("ticketPrice");

            if (!(seats instanceof Number) || !(price instanceof Number)) {
                throw new IllegalStateException(
                        "Inventory response is missing seat or price data");
            }

            return new InventoryDetails(
                    ((Number) seats).intValue(),
                    BigDecimal.valueOf(((Number) price).doubleValue()));

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Unable to contact Inventory Service", exception);
        }
    }

    public static class InventoryDetails {

        private final int availableSeats;
        private final BigDecimal ticketPrice;

        public InventoryDetails(int availableSeats, BigDecimal ticketPrice) {
            this.availableSeats = availableSeats;
            this.ticketPrice = ticketPrice;
        }

        public int getAvailableSeats() {
            return availableSeats;
        }

        public BigDecimal getTicketPrice() {
            return ticketPrice;
        }
    }
}
