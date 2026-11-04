/*  
202419094
Nguyen Chinh Thai
*/
package lab8;

public class AirConditionerRemote extends TeachingDevice {
    private final String assignedRoom;
    private boolean responsive = true;

    public AirConditionerRemote(String id, String name, String assignedRoom) {
        super(id, name);
        this.assignedRoom = requireText(assignedRoom, "assignedRoom");
    }

    public String getAssignedRoom() {
        return assignedRoom;
    }

    public boolean isResponsive() {
        return responsive;
    }

    public void updateResponseState(boolean isResponsive) {
        boolean wasResponsive = responsive;
        responsive = isResponsive;
        if (wasResponsive && !responsive) {
            raiseAlert("Air conditioner remote in " + assignedRoom + " is not responding", AlertSeverity.Critical);
        }
    }
}
