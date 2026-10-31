import java.util.Scanner;

/**
 * Menu-driven console application for managing lab equipment.
 *
 * Data is kept in two fixed-size arrays of Equipment objects:
 *   - inventory  : every registered item (max 5)
 *   - checkedOut : items currently checked out (max 3)
 *
 * Both arrays keep their active elements packed at the beginning
 * (indexes 0 .. count-1) followed by null slots. The checkedOut array
 * stores references to the SAME objects held in inventory (no copies),
 * so changing availability through either array is visible in both.
 */
public class EquipmentCheckoutSystem {

    private static final int MAX_INVENTORY = 5;
    private static final int MAX_CHECKED_OUT = 3;

    private static Equipment[] inventory = new Equipment[MAX_INVENTORY];
    private static Equipment[] checkedOut = new Equipment[MAX_CHECKED_OUT];
    private static int equipmentCount = 0;
    private static int checkedOutCount = 0;

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Simple Equipment Checkout System ===");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readMenuChoice();

            switch (choice) {
                case 1 -> addEquipment();
                case 2 -> checkOutEquipment();
                case 3 -> returnEquipment();
                case 4 -> viewInventory();
                case 5 -> viewCheckedOut();
                case 6 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> {
                    // readMenuChoice() already printed an error message
                }
            }
        }
        scanner.close();
    }

    // ------------------------------------------------------------------
    // Menu and input helpers
    // ------------------------------------------------------------------

    private static void printMenu() {
        System.out.println();
        System.out.println("----------- Menu -----------");
        System.out.println("1. Add Equipment");
        System.out.println("2. Check Out Equipment");
        System.out.println("3. Return Equipment");
        System.out.println("4. View Equipment Inventory");
        System.out.println("5. View Checked-Out Equipment");
        System.out.println("6. Exit");
        System.out.print("Enter your choice (1-6): ");
    }

    /**
     * Reads a menu choice. Returns 1-6 for a valid choice, or -1 after
     * printing an error for non-numeric or out-of-range input, so the
     * program never crashes on bad input.
     */
    private static int readMenuChoice() {
        String line = readLine();
        if (line == null) {
            // End of input (e.g. Ctrl+D): treat as Exit
            return 6;
        }
        line = line.trim();

        int choice;
        try {
            choice = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a number between 1 and 6.");
            return -1;
        }

        if (choice < 1 || choice > 6) {
            System.out.println("Error: Choice must be between 1 and 6.");
            return -1;
        }
        return choice;
    }

    /** Reads one line of input, or returns null if input has ended. */
    private static String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }

    /**
     * Prompts for a value, trims it and checks it is not empty.
     * Returns the trimmed value, or null (after printing an error) if it is
     * empty / whitespace only.
     */
    private static String readNonEmpty(String prompt, String fieldName) {
        System.out.print(prompt);
        String value = readLine();
        if (value == null || value.trim().isEmpty()) {
            System.out.println("Error: " + fieldName + " cannot be empty.");
            return null;
        }
        return value.trim();
    }

    // ------------------------------------------------------------------
    // Search helpers (linear search over the ACTIVE range only)
    // ------------------------------------------------------------------

    /** Returns the inventory item with the given ID (case-insensitive), or null. */
    private static Equipment findInInventory(String id) {
        for (int i = 0; i < equipmentCount; i++) {
            if (inventory[i].getEquipmentId().equalsIgnoreCase(id)) {
                return inventory[i];
            }
        }
        return null;
    }

    /** Returns the index of the item in checkedOut with the given ID, or -1. */
    private static int findCheckedOutIndex(String id) {
        for (int i = 0; i < checkedOutCount; i++) {
            if (checkedOut[i].getEquipmentId().equalsIgnoreCase(id)) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Menu operations
    // ------------------------------------------------------------------

    /** Option 1: register a new item at index equipmentCount. */
    private static void addEquipment() {
        if (equipmentCount == MAX_INVENTORY) {
            System.out.println("Error: Inventory is full (maximum " + MAX_INVENTORY + " items).");
            return;
        }

        String id = readNonEmpty("Enter equipment ID: ", "Equipment ID");
        if (id == null) {
            return;
        }
        if (findInInventory(id) != null) {
            System.out.println("Error: Equipment ID \"" + id + "\" already exists.");
            return;
        }

        String name = readNonEmpty("Enter equipment name: ", "Equipment name");
        if (name == null) {
            return;
        }

        // Place the new object right after the last active element
        Equipment item = new Equipment(id, name);
        inventory[equipmentCount] = item;
        equipmentCount++;
        System.out.println("Added: " + item);
    }

    /** Option 2: check out an available item from the inventory. */
    private static void checkOutEquipment() {
        if (equipmentCount == 0) {
            System.out.println("Error: No equipment in the inventory to check out.");
            return;
        }
        if (checkedOutCount == MAX_CHECKED_OUT) {
            System.out.println("Error: Cannot check out more than " + MAX_CHECKED_OUT
                    + " items at once. Return an item first.");
            return;
        }

        String id = readNonEmpty("Enter equipment ID to check out: ", "Equipment ID");
        if (id == null) {
            return;
        }

        Equipment item = findInInventory(id);
        if (item == null) {
            System.out.println("Error: Equipment \"" + id + "\" does not exist.");
            return;
        }
        if (!item.isAvailable()) {
            System.out.println("Error: " + item.getEquipmentId() + " is already checked out.");
            return;
        }

        // All checks passed: update state. The SAME object reference is stored
        // in checkedOut, so the inventory sees the availability change too.
        item.setAvailable(false);
        checkedOut[checkedOutCount] = item;
        checkedOutCount++;
        System.out.println("Checked out: " + item);
    }

    /** Option 3: return a checked-out item and remove it by shifting left. */
    private static void returnEquipment() {
        if (checkedOutCount == 0) {
            System.out.println("Error: No equipment is currently checked out.");
            return;
        }

        String id = readNonEmpty("Enter equipment ID to return: ", "Equipment ID");
        if (id == null) {
            return;
        }

        int index = findCheckedOutIndex(id);
        if (index == -1) {
            if (findInInventory(id) == null) {
                System.out.println("Error: Equipment \"" + id + "\" does not exist.");
            } else {
                System.out.println("Error: Equipment \"" + id + "\" is not checked out.");
            }
            return;
        }

        // Mark the shared object as available again (visible in inventory)
        Equipment item = checkedOut[index];
        item.setAvailable(true);

        // Manual shifting: move every element after 'index' one slot left,
        // overwriting the returned item and keeping active elements packed.
        // Example: [A, B, C] returning B (index 1) -> [A, C, C] -> [A, C, null]
        for (int i = index; i < checkedOutCount - 1; i++) {
            checkedOut[i] = checkedOut[i + 1];
        }

        // The last active slot is now a duplicate/unused position: clear it
        checkedOutCount--;
        checkedOut[checkedOutCount] = null;

        System.out.println("Returned: " + item);
    }

    /** Option 4: display every registered item using toString(). */
    private static void viewInventory() {
        if (equipmentCount == 0) {
            System.out.println("The inventory is empty.");
            return;
        }
        System.out.println("Equipment Inventory (" + equipmentCount + "/" + MAX_INVENTORY + "):");
        for (int i = 0; i < equipmentCount; i++) {
            System.out.println((i + 1) + ". " + inventory[i]);
        }
    }

    /** Option 5: display currently checked-out items using toString(). */
    private static void viewCheckedOut() {
        if (checkedOutCount == 0) {
            System.out.println("No equipment is currently checked out.");
            return;
        }
        System.out.println("Checked-Out Equipment (" + checkedOutCount + "/" + MAX_CHECKED_OUT + "):");
        for (int i = 0; i < checkedOutCount; i++) {
            System.out.println((i + 1) + ". " + checkedOut[i]);
        }
    }
}
