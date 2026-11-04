/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceAuditLogger {
    private final List<String> entries = new ArrayList<>();

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void handleStatusChanged(Object sender, DeviceStatusChangedEventArgs event) {
        entries.add(event.changedAt() + ": " + event.deviceId() + ", "
                + event.oldStatus() + " -> " + event.newStatus());
    }
}
