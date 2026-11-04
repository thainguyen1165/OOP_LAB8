/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

public class WirelessMicrophone extends TeachingDevice {
    private int batteryLevel = 100;
    private int signalQuality = 100;

    public WirelessMicrophone(String id, String name) {
        super(id, name);
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getSignalQuality() {
        return signalQuality;
    }

    public void updateBatteryLevel(int newLevel) {
        validateReading(newLevel);
        int oldLevel = batteryLevel;
        batteryLevel = newLevel;
        alertOnReadingTransition("Battery", oldLevel, newLevel, 30);
    }

    public void updateSignalQuality(int newQuality) {
        validateReading(newQuality);
        int oldQuality = signalQuality;
        signalQuality = newQuality;
        alertOnReadingTransition("Signal quality", oldQuality, newQuality, 40);
    }
}
