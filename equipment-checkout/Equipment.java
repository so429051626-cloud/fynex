/**
 * Represents a single piece of lab equipment (laptop, projector, camera, ...).
 *
 * Encapsulation: all fields are private, so other classes can only read or
 * change the state through the public methods below. The ID and name never
 * change after construction (no setters), while availability can be updated
 * when the item is checked out or returned.
 */
public class Equipment {

    private String equipmentId;
    private String name;
    private boolean available;

    /**
     * Creates a new equipment item. Every new item starts as available.
     * The caller is expected to pass already-validated, trimmed values.
     */
    public Equipment(String equipmentId, String name) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.available = true;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Returns the item in the required display format, for example:
     * "E101 - Projector ( Available )" or "E101 - Projector ( Checked Out )".
     */
    @Override
    public String toString() {
        String status = available ? "Available" : "Checked Out";
        return equipmentId + " - " + name + " ( " + status + " )";
    }
}
