/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

public class ConsoleNotificationService {
    public void handleAlert(Object sender, DeviceAlertEventArgs event) {
        System.out.printf("[%s] %s: %s%n", event.severity(), event.deviceId(), event.message());
    }
}
