package com.redheap.multiaction;

import javax.faces.event.FacesEvent;

public class MyBean {

    public void methodOne(FacesEvent event) {
        System.out.println(getClass().getName() + ".ONE: "  + event);
    }

    public void methodTwo(FacesEvent event) {
        System.out.println(getClass().getName() + ".TWO: " + event);
    }

    public void methodThree(FacesEvent event) {
        System.out.println(getClass().getName() + ".THREE: " + event);
    }

}
