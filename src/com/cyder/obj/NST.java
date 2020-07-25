package com.cyder.obj;

@SuppressWarnings("all")
public class NST {

    //A simple Named String Tag object for use anywhere

    private String name;
    private String description;

    public NST() {
        this.name = null;
        this.description = null;
    }

    public NST(String initName) {
        this.name = initName;
        this.description = null;
    }

    public NST(String initName, String initDesc) {
        this.name = initName;
        this.description = initDesc;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setDescription(String newDesc) {
        this.description = newDesc;
    }

    public void setNST(String newName, String newDesc) {
        this.name = newName;
        this.description = newDesc;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }
}