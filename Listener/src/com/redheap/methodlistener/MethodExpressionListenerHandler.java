package com.redheap.methodlistener;

import javax.el.ELException;
import javax.el.MethodExpression;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import oracle.adf.view.rich.event.BasePolytypeListener;

public class MethodExpressionListenerHandler extends TagHandler {

    private final TagAttribute method;
    private final TagAttribute type;

    public MethodExpressionListenerHandler(TagConfig tagConfig) {
        super(tagConfig);
        method = getRequiredAttribute("method");
        type = getRequiredAttribute("type");
    }

    public void apply(FaceletContext faceletContext, UIComponent parent) throws FaceletException, ELException {
        // only run on the first time the tag executes
        if (javax.faces.view.facelets.ComponentHandler.isNew(parent)) {
            if (parent == null) {
                throw new FaceletException("ActionListener must be inside UIComponent tag");
            }
            // build MethodExpression from "method" tag attribute. Should have no return value and accept a single
            // FacesEvent argument (or more likely and event specific subtype of FacesEvent)
            MethodExpression expression = method.getMethodExpression(faceletContext, Void.class, new Class[] {
                                                                     FacesEvent.class });

            // try to determine type (aka. when to fire)
            String type = this.type.getValue();
            BasePolytypeListener.EventType eventType = MethodExpressionListener.findEventType(type);
            if (eventType == null) {
                // the type is not valid, throw exception
                throw new FaceletException("ActionListener has invalid type attribute");
            }

            // create the listener and add it to the parent component
            MethodExpressionListener listener = new MethodExpressionListener(eventType);
            listener.setMethodExpression(expression);
            if (!MethodExpressionListener.addListener(listener, parent)) {
                throw new FaceletException("Failed to add listener to parent for type: " + eventType);
            }
        }
    }

}
