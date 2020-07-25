package com.cyder.obj;

@SuppressWarnings("all")
public class NBT {

    //A simple Named Binary Tag object for use anywhere

    private String name;
    private boolean binary;

    public NBT() {
        this.name = "null";
        this.binary = false;
    }

    public NBT(String initName) {
        this.name = initName;
        this.binary = false;
    }

    public NBT(boolean initBin) {
        this.name = "null";
        this.binary = initBin;
    }

    public NBT(String initName, boolean initBin) {
        this.name = initName;
        this.binary = initBin;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setBinary(boolean newBin) {
        this.binary = newBin;
    }

    public void setNBT(String newName, boolean newBin) {
        this.name = newName;
        this.binary = newBin;
    }

    public String getName() {
        return this.name;
    }

    public boolean getBinary() {
        return this.binary;
    }
}