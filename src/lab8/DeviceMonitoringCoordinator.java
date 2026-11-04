/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public class DeviceMonitoringCoordinator implements AutoCloseable {
    private final List<TeachingDevice> devices;
    private final EventHandler<DeviceAlertEventArgs> notificationHandler;
    private final EventHandler<DeviceStatusChangedEventArgs> auditHandler;
    private final EventHandler<DeviceAlertEventArgs> maintenanceHandler;
    private boolean subscribed;
    private boolean closed;

    public DeviceMonitoringCoordinator(List<? extends TeachingDevice> devices,
            ConsoleNotificationService notificationService, DeviceAuditLogger auditLogger,
            MaintenanceService maintenanceService) {
        this.devices = List.copyOf(new LinkedHashSet<>(devices));
        notificationHandler = Objects.requireNonNull(notificationService)::handleAlert;
        auditHandler = Objects.requireNonNull(auditLogger)::handleStatusChanged;
        maintenanceHandler = Objects.requireNonNull(maintenanceService)::handleCriticalAlert;
    }

    public void subscribe() {
        if (closed)
            throw new IllegalStateException("Coordinator is closed");
        if (subscribed)
            return;
        for (TeachingDevice device : devices) {
            device.addAlertRaisedListener(notificationHandler);
            device.addAlertRaisedListener(maintenanceHandler);
            device.addStatusChangedListener(auditHandler);
        }
        subscribed = true;
    }

    public void unsubscribe() {
        if (!subscribed)
            return;
        for (TeachingDevice device : devices) {
            device.removeAlertRaisedListener(notificationHandler);
            device.removeAlertRaisedListener(maintenanceHandler);
            device.removeStatusChangedListener(auditHandler);
        }
        subscribed = false;
    }

    @Override
    public void close() {
        unsubscribe();
        closed = true;
    }
}
