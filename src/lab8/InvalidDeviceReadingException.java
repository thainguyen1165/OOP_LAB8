/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

public class InvalidDeviceReadingException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final String deviceId;
    private final int invalidValue;

    public InvalidDeviceReadingException(String deviceId, int invalidValue) {
        super("Device " + deviceId + ": reading must be 0..100, received " + invalidValue);
        this.deviceId = deviceId;
        this.invalidValue = invalidValue;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getInvalidValue() {
        return invalidValue;
    }
}
