/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TeachingDevice {
    private final String id;
    private final String name;
    private DeviceStatus status = DeviceStatus.Available;
    private final List<EventHandler<DeviceStatusChangedEventArgs>> statusListeners = new ArrayList<>();
    private final List<EventHandler<DeviceAlertEventArgs>> alertListeners = new ArrayList<>();

    protected TeachingDevice(String id, String name) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void changeStatus(DeviceStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus");
        if (status == newStatus)
            return;
        DeviceStatus oldStatus = status;
        status = newStatus;
        onStatusChanged(new DeviceStatusChangedEventArgs(id, oldStatus, newStatus, LocalDateTime.now()));
    }

    public final void addStatusChangedListener(EventHandler<DeviceStatusChangedEventArgs> listener) {
        addListener(statusListeners, listener);
    }

    public final void removeStatusChangedListener(EventHandler<DeviceStatusChangedEventArgs> listener) {
        statusListeners.remove(listener);
    }

    public final void addAlertRaisedListener(EventHandler<DeviceAlertEventArgs> listener) {
        addListener(alertListeners, listener);
    }

    public final void removeAlertRaisedListener(EventHandler<DeviceAlertEventArgs> listener) {
        alertListeners.remove(listener);
    }

    private static <T> void addListener(List<EventHandler<T>> listeners, EventHandler<T> listener) {
        Objects.requireNonNull(listener, "listener");
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    protected void raiseAlert(String message, AlertSeverity severity) {
        onAlertRaised(new DeviceAlertEventArgs(id, message, severity, LocalDateTime.now()));
    }

    protected void onStatusChanged(DeviceStatusChangedEventArgs event) {
        dispatch(statusListeners, event);
    }

    protected void onAlertRaised(DeviceAlertEventArgs event) {
        dispatch(alertListeners, event);
    }

    private <T> void dispatch(List<EventHandler<T>> listeners, T event) {
        RuntimeException failure = null;
        // Chụp danh sách để handler có thể đăng ký/hủy trong lúc xử lý.
        for (EventHandler<T> listener : List.copyOf(listeners)) {
            try {
                listener.handle(this, event);
            } catch (RuntimeException exception) {
                if (failure == null)
                    failure = new IllegalStateException("Event listener failed for " + id);
                failure.addSuppressed(exception);
            }
        }
        if (failure != null)
            throw failure;
    }

    protected final void validateReading(int value) {
        if (value < 0 || value > 100)
            throw new InvalidDeviceReadingException(id, value);
    }

    protected final void alertOnReadingTransition(String reading, int oldValue, int newValue, int threshold) {
        AlertSeverity oldZone = readingZone(oldValue, threshold);
        AlertSeverity newZone = readingZone(newValue, threshold);
        if (oldZone != newZone && newZone != AlertSeverity.Information) {
            raiseAlert(reading + " is " + newValue + "%", newZone);
        }
    }

    private static AlertSeverity readingZone(int value, int threshold) {
        if (value == 0)
            return AlertSeverity.Critical;
        return value < threshold ? AlertSeverity.Warning : AlertSeverity.Information;
    }
}
