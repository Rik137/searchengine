package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base DTO for API responses
 * <p>
 * Used for all REST controller responses
 */

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ApiResponse {
    
    /**
    * Operation result (true — success, false — failure)
    */
    private boolean result;

    /**
    * Error message (null if the operation was successful)
    */
    private String error;
}
