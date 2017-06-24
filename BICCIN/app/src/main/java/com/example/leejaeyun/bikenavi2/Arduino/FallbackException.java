package com.example.leejaeyun.bikenavi2.Arduino;

/**
 * Created by Lee Jae Yun on 2017-05-20.
 */

public class FallbackException extends Exception {

    private static final long serialVersionUID = 1L;

    public FallbackException(Exception e) {
        super(e);
    }
}
