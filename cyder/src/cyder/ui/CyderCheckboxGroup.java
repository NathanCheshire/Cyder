package cyder.ui;

import java.util.LinkedList;

/**
 * A group of {@link CyderCheckbox}s in which only one may be selected at a time.
 */
public class CyderCheckboxGroup {
    /**
     * The list of managed checkboxes.
     */
    private final LinkedList<CyderCheckbox> checkboxes = new LinkedList<>();

    /**
     * The currently selected checkbox.
     */
    private CyderCheckbox currentlySelectedBox;

    /**
     * Constructs a new CyderCheckboxGroup object.
     */
    public CyderCheckboxGroup() {}

    /**
     * Clears all selected checkboxes in this group.
     */
    public void clearSelection() {
        if (checkboxes != null && currentlySelectedBox != null) {
            currentlySelectedBox.setNotSelected();
            currentlySelectedBox = null;
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
     * Sets the provided checkbox within the group to be selected.
     *
     * @param checkbox the checkbox to set as selected
     */
    public void setSelectedCheckbox(CyderCheckbox checkbox) {
        if (!checkboxes.contains(checkbox))
            throw new IllegalArgumentException("Provided CyderCheckbox is not in this group");

        currentlySelectedBox = checkbox;

        for (CyderCheckbox cb : checkboxes)
            if (cb != currentlySelectedBox)
                cb.setNotSelected();
    }

    /**
     * Finds the currently selected checkbox within this group.
     *
     * @return the currently selected checkbox
     */
    public CyderCheckbox getSelectedCheckbox() {
        return currentlySelectedBox;
    }

    /**
     * Finds how many checkboxes are associated with this group.
     *
     * @return the number of checkboxes in this group
     */
    public int getCheckboxCount() {
        if (checkboxes == null) {
            return 0;
        } else {
            return checkboxes.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (checkboxes == null || checkboxes.size() == 0)
            return "Empty checkbox group";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < getCheckboxCount() ; i++) {
            sb.append(checkboxes.get(i).toString());

            if (i != getCheckboxCount() - 1)
                sb.append(", ");
        }

        return sb.toString();
    }
}
