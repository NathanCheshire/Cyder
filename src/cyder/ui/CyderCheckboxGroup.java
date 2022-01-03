package cyder.ui;

import java.util.LinkedList;

public class CyderCheckboxGroup {
    private LinkedList<CyderCheckbox> checkboxes = new LinkedList<>();
    private CyderCheckbox currentlySelectedBox;

    public CyderCheckboxGroup() {
        //constructor to allow object creation
    }

    /**
     * Sets the currently selected box in this group to not selected
     */
    public void clearSelection() {
        if (checkboxes != null && currentlySelectedBox != null) {
            currentlySelectedBox.setNotSelected();
            currentlySelectedBox = null;
        }
    }

    /**
     * Adds the given checkbox to the group
     * @param box the checkbox to add
     */
    public void addCheckbox(CyderCheckbox box) {
        if (checkboxes.contains(box))
            return;

        checkboxes.add(box);
        box.setCyderCheckboxGroup(this);
    }

    /**
     * removes the provided checkbox from the group
     * @param box the box to remove from the group
     */
    public void removeCheckbox(CyderCheckbox box) {
        checkboxes.remove(box);
        box.setCyderCheckboxGroup(null);
    }

    /**
     * Sets the provided checkbox within the group to be selected
     * @param box the checkbox to set as selected
     */
    public void setSelectedCheckbox(CyderCheckbox box) {
        if (!checkboxes.contains(box))
            throw new IllegalArgumentException("Provided CyderCheckbox is not in this button group");

        currentlySelectedBox = box;

        System.out.println("here");

        for (CyderCheckbox cb : checkboxes)
            if (cb != currentlySelectedBox)
                cb.setNotSelected();
    }

    /**
     * Finds the currently selected checkbox within this button group
     * @return the currently selected checkbox
     */
    public CyderCheckbox getSelectedCheckbox() {
        return currentlySelectedBox;
    }

    /**
     * Finds how many buttons are associated with this button group
     * @return the number of checkboxes in this button group
     */
    public int getButtonCount() {
        if (checkboxes == null) {
            return 0;
        } else {
            return checkboxes.size();
        }
    }

    @Override
    public String toString() {
        if (checkboxes == null || checkboxes.size() == 0)
            return "Empty checkbox group";

        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < getButtonCount() ; i++) {
            sb.append(checkboxes.get(i).toString());

            if (i != getButtonCount() - 1)
                sb.append(", ");
        }

        return sb.toString();
    }
}
