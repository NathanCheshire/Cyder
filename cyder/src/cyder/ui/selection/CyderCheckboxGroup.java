package cyder.ui.selection;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.LinkedList;

/**
 * A group of {@link CyderCheckbox}s in which only one may be checked at a time.
 */
public class CyderCheckboxGroup {
    /**
     * The list of checkboxes.
     */
    private final LinkedList<CyderCheckbox> checkboxes = new LinkedList<>();

    /**
     * The currently checked checkbox.
     */
    private CyderCheckbox currentlyCheckedBox;

    /**
     * Constructs a new CyderCheckboxGroup object.
     */
    public CyderCheckboxGroup() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Clears all checked checkboxes in this group.
     */
    public void clearSelection() {
        if (currentlyCheckedBox != null) {
            currentlyCheckedBox.setNotChecked();
            currentlyCheckedBox = null;
        }
    }

    /**
     * Adds the given checkbox to the group.
     *
     * @param checkbox the checkbox to add
     */
    public void addCheckbox(CyderCheckbox checkbox) {
        if (checkboxes.contains(checkbox))
            return;

        checkboxes.add(checkbox);
        checkbox.setCyderCheckboxGroup(this);
    }

    /**
     * removes the provided checkbox from the group.
     *
     * @param checkbox the checkbox to remove from the group
     */
    public void removeCheckbox(CyderCheckbox checkbox) {
        checkboxes.remove(checkbox);
        checkbox.setCyderCheckboxGroup(null);
    }

    /**
     * Sets the provided checkbox within the group to be checked.
     *
     * @param checkbox the checkbox to set as checked
     */
    public void setCheckedBox(CyderCheckbox checkbox) {
        Preconditions.checkNotNull(checkbox);
        Preconditions.checkArgument(checkboxes.contains(checkbox));

        currentlyCheckedBox = checkbox;

        refreshNonOwnerBoxes();
    }

    /**
     * Calls setNotChecked on all checkboxes not equal to the currently checked box of this group.
     */
    public void refreshNonOwnerBoxes() {
        checkboxes.forEach(checkBox -> {
            if (checkBox != currentlyCheckedBox) {
                checkBox.setNotChecked();
            }
        });
    }

    /**
     * Disables all checkboxes in this group.
     */
    public void disableCheckboxes() {
        checkboxes.forEach(checkBox -> checkBox.setEnabled(false));
    }

    /**
     * Enables all checkboxes in this group.
     */
    public void enableCheckboxes() {
        checkboxes.forEach(checkBox -> checkBox.setEnabled(true));
    }

    /**
     * Finds the currently checked checkbox within this group.
     *
     * @return the currently checked checkbox
     */
    public CyderCheckbox getCheckedCheckbox() {
        return currentlyCheckedBox;
    }

    /**
     * Finds how many checkboxes are associated with this group.
     *
     * @return the number of checkboxes in this group
     */
    public int getCheckboxCount() {
        return checkboxes.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (checkboxes.isEmpty()) {
            return "Empty checkbox group";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < getCheckboxCount() ; i++) {
            sb.append(checkboxes.get(i).toString());

            if (i != getCheckboxCount() - 1) sb.append(", ");
        }

        return sb.toString();
    }
}
