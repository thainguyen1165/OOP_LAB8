/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaintenanceService {
    private final List<String> requests = new ArrayList<>();

    public List<String> getRequests() {
        return Collections.unmodifiableList(requests);
    }

    public void handleCriticalAlert(Object sender, DeviceAlertEventArgs event) {
        if (event.severity() == AlertSeverity.Critical) {
            requests.add("Maintenance request for " + event.deviceId() + ": " + event.message());
        }
    }
}
