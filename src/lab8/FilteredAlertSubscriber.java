/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

import java.util.Objects;
import java.util.function.Consumer;

public class FilteredAlertSubscriber {
    private final AlertFilter filter;
    private final Consumer<DeviceAlertEventArgs> action;

    public FilteredAlertSubscriber(AlertFilter filter, Consumer<DeviceAlertEventArgs> action) {
        this.filter = Objects.requireNonNull(filter, "filter");
        this.action = Objects.requireNonNull(action, "action");
    }

    public void handleAlert(Object sender, DeviceAlertEventArgs event) {
        if (filter.test(event))
            action.accept(event);
    }
}
