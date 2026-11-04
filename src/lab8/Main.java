/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Main {
    public static void main(String[] args) {
        runTests();
        runDemo();
    }

    private static void runDemo() {
        ProjectorRemote projector = new ProjectorRemote("PR-01", "Projector remote");
        AirConditionerRemote aircon = new AirConditionerRemote("AC-01", "AC remote", "A101");
        WirelessMicrophone microphone = new WirelessMicrophone("MIC-01", "Wireless microphone");
        List<TeachingDevice> devices = List.of(projector, aircon, microphone);
        DeviceAuditLogger audit = new DeviceAuditLogger();
        MaintenanceService maintenance = new MaintenanceService();
        FilteredAlertSubscriber criticalOnly = new FilteredAlertSubscriber(
                alert -> alert.severity() == AlertSeverity.Critical,
                alert -> System.out.println("CALLBACK CRITICAL: " + alert.deviceId() + " - " + alert.message()));
        EventHandler<DeviceAlertEventArgs> filteredHandler = criticalOnly::handleAlert;

        try (DeviceMonitoringCoordinator coordinator = new DeviceMonitoringCoordinator(
                devices, new ConsoleNotificationService(), audit, maintenance)) {
            coordinator.subscribe();
            coordinator.subscribe(); // Không tạo đăng ký trùng.
            devices.forEach(device -> device.addAlertRaisedListener(filteredHandler));
            try {
                System.out.println("=== Status changes ===");
                projector.changeStatus(DeviceStatus.Borrowed);
                projector.changeStatus(DeviceStatus.Borrowed); // Không phát event.
                projector.changeStatus(DeviceStatus.Available);

                System.out.println("=== Device alerts ===");
                projector.updateBatteryLevel(20);
                projector.updateBatteryLevel(19);
                projector.updateBatteryLevel(10); // Cùng vùng Warning: không lặp.
                projector.updateBatteryLevel(0);
                aircon.updateResponseState(false);
                aircon.updateResponseState(false); // Không lặp.
                microphone.updateBatteryLevel(29);
                microphone.updateSignalQuality(39);
                microphone.updateSignalQuality(0);
                microphone.updateBatteryLevel(0);
                microphone.changeStatus(DeviceStatus.UnderMaintenance);

                System.out.println("=== Invalid reading ===");
                try {
                    microphone.updateBatteryLevel(101);
                } catch (InvalidDeviceReadingException exception) {
                    System.out.println(exception.getMessage());
                }
            } finally {
                devices.forEach(device -> device.removeAlertRaisedListener(filteredHandler));
            }
        }

        System.out.println("=== Audit history ===");
        audit.getEntries().forEach(System.out::println);
        System.out.println("=== Maintenance requests ===");
        maintenance.getRequests().forEach(System.out::println);
        System.out.println("=== After close: no subscriber output expected ===");
        aircon.updateResponseState(true);
        aircon.updateResponseState(false);
    }

    private static int checks;

    private static void runTests() {
        checks = 0;
        System.out.println("=== Tests ===");
        ProjectorRemote projector = new ProjectorRemote("P", "Projector");
        checkReading(projector, projector::updateBatteryLevel, projector::getBatteryLevel, 20);
        WirelessMicrophone mic = new WirelessMicrophone("M", "Microphone");
        checkReading(mic, mic::updateBatteryLevel, mic::getBatteryLevel, 30);
        checkReading(mic, mic::updateSignalQuality, mic::getSignalQuality, 40);
        checkStatus();
        checkAircon();
        checkCoordinatorAndFilter();
        checkListenerFailure();
        System.out.println("PASS: " + checks + " checks");
    }

    private static void checkReading(TeachingDevice device, IntConsumer update, IntSupplier reading, int threshold) {
        List<AlertSeverity> alerts = new ArrayList<>();
        EventHandler<DeviceAlertEventArgs> handler = (sender, event) -> {
            check(sender == device && event.deviceId().equals(device.getId()), "event sender/id");
            check(event.occurredAt() != null && !event.message().isBlank(), "alert data");
            alerts.add(event.severity());
        };
        device.addAlertRaisedListener(handler);
        device.addAlertRaisedListener(handler);
        update.accept(100);
        update.accept(threshold);
        check(alerts.isEmpty(), "threshold is safe");
        update.accept(threshold - 1);
        update.accept(1);
        check(alerts.equals(List.of(AlertSeverity.Warning)), "one warning per zone");
        update.accept(0);
        update.accept(0);
        check(alerts.equals(List.of(AlertSeverity.Warning, AlertSeverity.Critical)), "zero critical once");
        update.accept(1);
        check(alerts.size() == 3 && alerts.get(2) == AlertSeverity.Warning, "critical to warning");
        update.accept(100);
        check(alerts.size() == 3, "safe recovery is silent");
        update.accept(0);
        check(alerts.size() == 4 && alerts.get(3) == AlertSeverity.Critical, "direct safe to critical");
        for (int invalid : new int[] { -1, 101 }) {
            try {
                update.accept(invalid);
                throw new AssertionError("Invalid reading accepted");
            } catch (InvalidDeviceReadingException exception) {
                check(exception.getDeviceId().equals(device.getId()) && exception.getInvalidValue() == invalid,
                        "exception data");
                check(reading.getAsInt() == 0 && alerts.size() == 4, "invalid reading has no side effects");
            }
        }
        device.removeAlertRaisedListener(handler);
        update.accept(10);
        check(alerts.size() == 4, "listener removed");
    }

    private static void checkStatus() {
        ProjectorRemote device = new ProjectorRemote("S", "Status");
        List<DeviceStatusChangedEventArgs> events = new ArrayList<>();
        device.addStatusChangedListener((sender, event) -> {
            check(device.getStatus() == event.newStatus(), "state updated before dispatch");
            check(sender == device && event.deviceId().equals("S") && event.changedAt() != null, "status data");
            events.add(event);
        });
        device.changeStatus(DeviceStatus.Available);
        check(events.isEmpty(), "unchanged status is silent");
        device.changeStatus(DeviceStatus.Borrowed);
        device.changeStatus(DeviceStatus.Borrowed);
        device.changeStatus(DeviceStatus.UnderMaintenance);
        device.changeStatus(DeviceStatus.Available);
        check(events.size() == 3 && events.get(0).oldStatus() == DeviceStatus.Available,
                "all status transitions");
    }

    private static void checkAircon() {
        AirConditionerRemote device = new AirConditionerRemote("A", "AC", "A101");
        List<DeviceAlertEventArgs> events = new ArrayList<>();
        device.addAlertRaisedListener((sender, event) -> {
            check(!device.isResponsive(), "response updated before dispatch");
            events.add(event);
        });
        device.updateResponseState(true);
        device.updateResponseState(false);
        device.updateResponseState(false);
        device.updateResponseState(true);
        device.updateResponseState(false);
        check(events.size() == 2 && events.stream().allMatch(e -> e.severity() == AlertSeverity.Critical),
                "AC alerts only on failure transitions");
    }

    private static void checkCoordinatorAndFilter() {
        ProjectorRemote device = new ProjectorRemote("C", "Coordinator");
        DeviceAuditLogger audit = new DeviceAuditLogger();
        MaintenanceService maintenance = new MaintenanceService();
        int[] notifications = { 0 };
        ConsoleNotificationService console = new ConsoleNotificationService() {
            @Override
            public void handleAlert(Object sender, DeviceAlertEventArgs event) {
                notifications[0]++;
            }
        };
        List<DeviceAlertEventArgs> filtered = new ArrayList<>();
        FilteredAlertSubscriber subscriber = new FilteredAlertSubscriber(
                e -> e.severity() == AlertSeverity.Critical, filtered::add);
        EventHandler<DeviceAlertEventArgs> external = subscriber::handleAlert;
        device.addAlertRaisedListener(external);
        DeviceMonitoringCoordinator coordinator = new DeviceMonitoringCoordinator(
                List.of(device, device), console, audit, maintenance);
        coordinator.unsubscribe();
        coordinator.subscribe();
        coordinator.subscribe();
        device.changeStatus(DeviceStatus.Borrowed);
        device.updateBatteryLevel(19);
        check(maintenance.getRequests().isEmpty() && filtered.isEmpty(), "warning filtered out");
        device.updateBatteryLevel(0);
        check(notifications[0] == 2 && maintenance.getRequests().size() == 1 && filtered.size() == 1
                && audit.getEntries().size() == 1, "multiple subscribers, no duplicate subscriptions");
        expectUnsupported(() -> audit.getEntries().clear());
        expectUnsupported(() -> maintenance.getRequests().clear());
        coordinator.unsubscribe();
        coordinator.unsubscribe();
        device.updateBatteryLevel(100);
        device.updateBatteryLevel(0);
        device.changeStatus(DeviceStatus.Available);
        check(notifications[0] == 2 && maintenance.getRequests().size() == 1
                && audit.getEntries().size() == 1 && filtered.size() == 2, "unsubscribe preserves external listener");
        coordinator.subscribe();
        device.changeStatus(DeviceStatus.Borrowed);
        check(audit.getEntries().size() == 2, "resubscribe works");
        coordinator.close();
        coordinator.close();
        device.changeStatus(DeviceStatus.Available);
        check(audit.getEntries().size() == 2, "close unsubscribes");
        try {
            coordinator.subscribe();
            throw new AssertionError("Closed coordinator accepted subscription");
        } catch (IllegalStateException expected) {
            checks++;
        }
        device.removeAlertRaisedListener(external);
    }

    private static void checkListenerFailure() {
        ProjectorRemote device = new ProjectorRemote("F", "Failure");
        int[] received = { 0 };
        device.addAlertRaisedListener((sender, event) -> {
            throw new IllegalArgumentException("broken handler");
        });
        device.addAlertRaisedListener((sender, event) -> received[0]++);
        try {
            device.updateBatteryLevel(0);
            throw new AssertionError("Listener failure not reported");
        } catch (IllegalStateException exception) {
            check(exception.getSuppressed().length == 1, "listener exception retained");
        }
        check(received[0] == 1 && device.getBatteryLevel() == 0, "remaining listener receives committed state");
    }

    private static void expectUnsupported(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Collection is mutable");
        } catch (UnsupportedOperationException expected) {
            checks++;
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition)
            throw new AssertionError(message);
        checks++;
    }
}
