/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

public class ProjectorRemote extends TeachingDevice {
    private int batteryLevel = 100;

    public ProjectorRemote(String id, String name) {
        super(id, name);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void updateBatteryLevel(int newLevel) {
        validateReading(newLevel);
        int oldLevel = batteryLevel;
        batteryLevel = newLevel;
        alertOnReadingTransition("Battery", oldLevel, newLevel, 20);
    }
}
