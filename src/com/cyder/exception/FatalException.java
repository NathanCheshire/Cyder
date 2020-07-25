package com.cyder.exception;

//TODO use this where code should never be and throw it there, ex: if we have only 3 vals and we test each
//and then we have an else and we throw this there
public class FatalException extends Exception{
    public FatalException(String s) {
        super(s);
    }
}
