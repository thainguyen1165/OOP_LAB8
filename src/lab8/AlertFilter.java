/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

@FunctionalInterface
public interface AlertFilter {
    boolean test(DeviceAlertEventArgs alert);
}
