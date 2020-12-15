package com.cyder.obj;

public class NST {

    //A simple Named String Tag object for use anywhere

    private String name;
    private String data;

    public NST() {
        this.name = null;
        this.data = null;
    }

    public NST(String initName) {
        this.name = initName;
        this.data = null;
    }

    public NST(String initName, String initDesc) {
        this.name = initName;
        this.data = initDesc;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setData(String newDesc) {
        this.data = newDesc;
    }

    public void setNST(String newName, String newDesc) {
        this.name = newName;
        this.data = newDesc;
    }

    public String getName() {
        return this.name;
    }

    public String getData() {
        return this.data;
    }
}