package com.redheap.multiaction;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;

public class MyBean {

    private UIComponent inputText;

    public void methodOne(FacesEvent event) {
        System.out.println(getClass().getName() + ".ONE: " + event);
    }

    public void methodTwo(FacesEvent event) {
        System.out.println(getClass().getName() + ".TWO: " + event);
    }

    public void methodThree(FacesEvent event) {
        System.out.println(getClass().getName() + ".THREE: " + event);
    }

    public void letItFail(FacesEvent event) {
        System.out.println(getClass().getName() + ".FAILING: " + event);
        throw new RuntimeException("not doing this");
    }

    public void setSomeProperty(String s) {
        System.out.println("setSomeProperty: " + s);
        if ("three".equals(s)) {
            throw new RuntimeException("three is a crowd");
        }
    }

    private String failure;
    private String okay;


    public void setFailure(String failure) {
        System.out.println("setFailure " + failure);
        this.failure = failure;
        throw new RuntimeException("not doing this");
    }

    public String getFailure() {
        return failure;
    }

    public void setOkay(String okay) {
        System.out.println("setOkay " + okay);
        this.okay = okay;
    }

    public String getOkay() {
        return okay;
    }

    public void failureMethod(FacesEvent event) {
        System.out.println("failureMethod " + event);
        throw new RuntimeException("not doing this");
    }

    public void okayMethod(FacesEvent event) {
        System.out.println("okayMethod " + event);
    }
}
