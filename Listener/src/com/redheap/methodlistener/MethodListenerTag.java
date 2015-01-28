package com.redheap.methodlistener;

import javax.el.MethodExpression;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletException;
import javax.faces.webapp.UIComponentClassicTagBase;

import javax.servlet.jsp.JspException;

import oracle.adf.view.rich.event.BasePolytypeListener;

import org.apache.myfaces.trinidad.webapp.TrinidadTagSupport;

public class MethodListenerTag extends TrinidadTagSupport {

    private MethodExpression method;
    private String type;

    private BasePolytypeListener.EventType eventType;

    public void setMethod(MethodExpression method) {
        this.method = method;
    }

    public void setType(String type) {
        BasePolytypeListener.EventType t = MethodListener.findEventType(type);
        if (t != null) {
            this.type = type;
            this.eventType = t;
            return;
        }
        // the type is not valid, throw exception
        throw new IllegalArgumentException("invalid type attribute for ActionListener");
    }

    @Override
    public int doStartTag() throws JspException {
        UIComponentClassicTagBase tag = UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (tag == null) {
            throw new JspException("ActionListener must be inside a UIComponent tag");
        }

        // only run on the first time the tag executes
        if (!tag.getCreated()) {
            return SKIP_BODY;
        }

        UIComponent component = tag.getComponentInstance();
        // create the listener and add it to the parent component
        MethodListener listener = new MethodListener(eventType);
        if (method != null) {
            listener.setMethodExpression(method);
        }
        if (!MethodListener.addListener(listener, component)) {
            throw new FaceletException("Failed to add listener to parent for type: " + eventType);
        }
        return super.doStartTag();
    }

    @Override
    public void release() {
        super.release();
        method = null;
        type = null;
        eventType = null;
    }

}
