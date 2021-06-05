package cyder.obj;

public class NBT {

    //A simple Named Binary Tag object for use anywhere

    private String name;
    private boolean data;

    public NBT() {
        this.name = "null";
        this.data = false;
    }

    public NBT(String initName) {
        this.name = initName;
        this.data = false;
    }

    public NBT(boolean initBin) {
        this.name = "null";
        this.data = initBin;
    }

    public NBT(String initName, boolean initBin) {
        this.name = initName;
        this.data = initBin;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setData(boolean newBin) {
        this.data = newBin;
    }

    public void setNBT(String newName, boolean newBin) {
        this.name = newName;
        this.data = newBin;
    }

    public String getName() {
        return this.name;
    }

    public boolean getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "NBT object (" +
                this.getName() + "," +
                this.getData() +
                "), hash=" + this.hashCode();
    }
}