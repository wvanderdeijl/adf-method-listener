package com.redheap.methodlistener;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletException;
import javax.faces.webapp.UIComponentClassicTagBase;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspFactory;

import oracle.adf.view.rich.event.BasePolytypeListener;

import org.apache.myfaces.trinidad.webapp.TrinidadTagSupport;

public class MethodExpressionListenerTag extends TrinidadTagSupport {

    private MethodExpression method;
    private String type;

    private BasePolytypeListener.EventType eventType;

    public void setMethod(MethodExpression method) {
        this.method = method;
    }

    public void setType(String type) {
        BasePolytypeListener.EventType t = MethodExpressionListener.findEventType(type);
        if (t != null) {
            this.type = type;
            this.eventType = t;
            return;
        }
        // the type is not valid, throw exception
        throw new IllegalArgumentException("Unable to find FacesEvent for type attribute " + type);
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
        MethodExpressionListener listener = new MethodExpressionListener(eventType);
        if (method != null) {
            // TODO: cleanup
            final ServletContext servletCtx = this.pageContext.getServletContext();
            final JspApplicationContext appCtx = JspFactory.getDefaultFactory().getJspApplicationContext(servletCtx);
            ExpressionFactory expressionFactory = appCtx.getExpressionFactory();
            MethodExpression noArgs =
                expressionFactory.createMethodExpression(this.pageContext.getELContext(), method.getExpressionString(),
                                                         null, new Class[] { });
            MethodExpression oneArg =
                expressionFactory.createMethodExpression(this.pageContext.getELContext(), method.getExpressionString(),
                                                         null, new Class[] { eventType.getEventClass() });
            listener.setNoArgumentMethodExpression(noArgs);
            listener.setSingleArgumentMethodExpression(oneArg);
        }
        if (!MethodExpressionListener.addListener(listener, component)) {
            throw new FaceletException("Failed to add listener for type " + eventType + " to parent " + component);
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
