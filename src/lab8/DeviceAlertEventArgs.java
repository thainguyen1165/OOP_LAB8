/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.time.LocalDateTime;

public record DeviceAlertEventArgs(String deviceId, String message,
                AlertSeverity severity, LocalDateTime occurredAt) {
}
