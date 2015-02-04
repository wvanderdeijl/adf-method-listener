package com.redheap.methodlistener;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentClassicTagBase;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspApplicationContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspFactory;

import oracle.adf.view.rich.event.BasePolytypeListener;

import org.apache.myfaces.trinidad.webapp.TrinidadTagSupport;

/**
 * JSP tag handler for rh:methodExpressionListener. Will process the JSP tags and create a MethodExpressionListener
 * that is attached to the parent JSF component.
 * @see #MethodExpressionListener
 */
public class MethodExpressionListenerTag extends TrinidadTagSupport {

    // JSP tag attribute values
    private MethodExpression method;
    private BasePolytypeListener.EventType eventType;

    /**
     * Set the JSP tag {@code method} attribute.
     * @param method MethodExpression, for example {@code #{myBean.someMethod}}
     */
    public void setMethod(MethodExpression method) {
        this.method = method;
    }

    /**
     * Set the JSP tag {@code type} attribute.
     * @param type type which should be one of the types supported by MethodExpressionListener and thus its super class
     * BasePolytypeListener
     * @see MethodExpressionListener
     */
    public void setType(String type) {
        BasePolytypeListener.EventType t = MethodExpressionListener.findEventType(type);
        if (t != null) {
            this.eventType = t;
            return;
        }
        // the type is not valid, throw exception
        throw new IllegalArgumentException("Unable to find FacesEvent for type attribute " + type);
    }

    /**
     * Process the start tag for this instance which will create the needed MethodExpressionListener and attach
     * it to the parent UIComponent.
     * @return SKIP_BODY as this tag never contains a body that needs processing
     * @throws JspException
     */
    @Override
    public int doStartTag() throws JspException {
        UIComponentClassicTagBase parentTag = UIComponentClassicTagBase.getParentUIComponentClassicTagBase(pageContext);
        if (parentTag == null) {
            throw new JspException("MethodExpressionListener must be inside a UIComponent tag");
        }
        if (!parentTag.getCreated()) {
            // skip when parent tag hasn't just created its component
            return SKIP_BODY;
        }
        // create the listener and add it to the parent component
        attachListener(parentTag.getComponentInstance());
        return super.doStartTag();
    }

    private void attachListener(final UIComponent component) throws JspException {
        MethodExpressionListener listener = new MethodExpressionListener(eventType);
        if (method != null) {
            final ELContext elCtx = this.pageContext.getELContext();
            final ExpressionFactory expressionFactory = getExpressionFactory();
            final String expression = method.getExpressionString();
            // build new type-safe MethodExpressions instead of generic single FacesEvent argument supplied by JSP tag
            MethodExpression noArgs = expressionFactory.createMethodExpression(elCtx, expression, null, new Class[] {
                                                                               });
            MethodExpression oneArg = expressionFactory.createMethodExpression(elCtx, expression, null, new Class[] {
                                                                               eventType.getEventClass() });
            listener.setNoArgumentMethodExpression(noArgs);
            listener.setSingleArgumentMethodExpression(oneArg);
        }
        // attach new listener to parent component
        if (!MethodExpressionListener.addListener(listener, component)) {
            throw new JspException("Failed to add listener for type " + eventType + " to parent " + component);
        }
    }

    /**
     * Find the ExpressionFactory from the pageContext.
     * @return ExpressionFactory
     */
    private ExpressionFactory getExpressionFactory() {
        final ServletContext servletCtx = this.pageContext.getServletContext();
        final JspApplicationContext appCtx = JspFactory.getDefaultFactory().getJspApplicationContext(servletCtx);
        return appCtx.getExpressionFactory();
    }

    /**
     * Called on a Tag handler to release state. The page compiler guarantees that JSP page implementation objects will
     * invoke this method on all tag handlers, but there may be multiple invocations on doStartTag and doEndTag in
     * between.
     */
    @Override
    public void release() {
        super.release();
        method = null;
        eventType = null;
    }

}
