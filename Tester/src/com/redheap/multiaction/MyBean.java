package com.redheap.multiaction;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;

public class MyBean {

    private String failure;
    private String okay;

    public void okayActionListener(ActionEvent event) {
        System.out.println("okayActionListener " + event);
    }

    public void anotherOkayActionListener(ActionEvent event) {
        System.out.println("anotherOkayActionListener " + event);
    }

    public void failureActionListener(ActionEvent event) {
        System.out.println("failureActionListener " + event);
        throw new RuntimeException("always throwing exception in failureActionListener");
    }

    public void silentFailureActionListener(ActionEvent event) throws AbortProcessingException {
        System.out.println("silentFailureActionListener " + event);
        throw new AbortProcessingException("always throwing exception in silentFailureActionListener");
    }

    public void silentFailureAndMessageActionListener(ActionEvent event) throws AbortProcessingException {
        System.out.println("silentFailureAndMessageActionListener " + event);
        FacesContext.getCurrentInstance().addMessage(null,
                                                     new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                                                      "Some error message"));
        throw new AbortProcessingException("always throwing exception in silentFailureAndMessageActionListener");
    }

    public void popupFetchedOne(PopupFetchEvent event) {
        System.out.println("popupFetchedOne " + event);
    }

    public void popupFetchedTwo(PopupFetchEvent event) {
        System.out.println("popupFetchedTwo " + event);
    }

    public void dialogOne(DialogEvent event) {
        System.out.println("dialogOne " + event);
    }

    public void dialogTwo(DialogEvent event) {
        System.out.println("dialogOne " + event);
    }

    public void setFailure(String failure) {
        System.out.println("setFailure " + failure);
        this.failure = failure;
        throw new RuntimeException("always throwing exception in setFailure");
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

}
