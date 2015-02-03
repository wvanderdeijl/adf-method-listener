package com.redheap.methodlistener;

import java.util.List;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.event.BasePolytypeListener;

import org.apache.myfaces.trinidad.bean.PropertyKey;

/**
 * Faces event listener that can register with all ADF and JSF server-side faces events and executes a MethodExpression
 * when executed.  The event type passed as a constructor parameter will determine what faces event the listener will
 * act on. See {@link BasePolytypeListener} for a list of supported event types.<p>
 * The MethodExpression specified in the {@code method} attribute should either accept no arguments at all or a
 * single argument of the correct event type. This event type argument is the name of the {@code type} attribute
 * with {@code Event} appended. For example @{code type="action"} would require a listener accepting a
 * {@code ActionEvent}
 * <p>
 * Since this JSF tag is a child of the component it is attaching to, it allows for multiple listeners to attach
 * to the same component. This is something that is not possible with the normal {@code actionListener} or other
 * listener attributes on the component itself.
 * <p>
 * Example usages:<br>
 * {@code <af:button><rh:methodExpressionListener type="action" method="#{myBean.clicked}"/></af:button>} <br>
 * {@code <af:button><rh:methodExpressionListener type="action" method="#{bindings.Commit.execute}"/></af:button>} <br>
 * {@code <af:dialog><rh:methodExpressionListener type="dialog" method="#{myBean.dialogListener}"/></af:dialog>}
 * <p>
 * This code is inspired by oracle.adf.view.rich.event.SetPropertyListener,
 * oracle.adfinternal.view.faces.facelets.rich.SetPropertyListenerHandler and
 * oracle.adfinternal.view.faces.taglib.listener.SetPropertyListenerTag
 */
public class MethodExpressionListener extends BasePolytypeListener {

    // property keys to store propertys in internal FacesBean that takes care of state saving and restore
    private static final PropertyKey NOARG_METHOD_KEY = PropertyKey.createPropertyKey("noarg-method");
    private static final PropertyKey ONEARG_METHOD_KEY = PropertyKey.createPropertyKey("onearg-method");

    private static final ADFLogger logger = ADFLogger.createADFLogger(MethodExpressionListener.class);

    /**
     * The no-args constructor default type is {@link BasePolytypeListener.EventType#ACTION}.
     */
    public MethodExpressionListener() {
        super(BasePolytypeListener.EventType.ACTION);
    }

    /**
     * Constructor requires an enumeration of {@link BasePolytypeListener.EventType} that defines
     * what associated event this particular instance handles.
     * @param eventType framework concrete event
     * @throws IllegalArgumentException eventType cannot be null
     */
    public MethodExpressionListener(BasePolytypeListener.EventType eventType) {
        super(eventType);
    }

    /**
     * Sets the "from" or "to" property via a value expression.
     *
     * @param name "from" or "to"
     * @param binding Tag EL binding
     * @throws IllegalArgumentException name is invalid for this state holder; supports 'from' or 'to'
     */
    public void setMethodExpression(MethodExpression noArgMethod, MethodExpression oneArgMethod) {
        getFacesBean().setProperty(NOARG_METHOD_KEY, noArgMethod);
        getFacesBean().setProperty(ONEARG_METHOD_KEY, oneArgMethod);
    }

    /**
     * Gets the "from" or "to" property as a value expression.
     *
     * @param name "from" or "to"
     * @throws IllegalArgumentException name is invalid for this state holder; supports 'from' or 'to'
     */
    //        public String getMethodExpressionString() {
    //            return (MethodExpression) getFacesBean().getProperty(METHOD_KEY);
    //        }

    /**
     * Invoked from the super class with the event matches the target {@link BasePolytypeListener.EventType}.
     * Delegates event handling to the methodExpression supplied to {@link #setMethodExpression}
     * @param event target faces event
     */
    @Override
    protected void handleEvent(final FacesEvent event) throws AbortProcessingException {
        int numExceptionsBefore = getBindingContainerExceptionCount();
        if (!executeOneArgExpression(event)) {
            // methodExpression doesn't seem to be for a single-arg method, retry no-arg version
            if (!executeNoArgExpression()) {
                final MethodExpression expression = (MethodExpression) getFacesBean().getProperty(ONEARG_METHOD_KEY);
                throw new MethodNotFoundException(expression.getExpressionString() +
                                                  " not resolving to no-arg method or method accepting a single " +
                                                  getEventType().getEventClass().getName());
            }
        }
        if (getBindingContainerExceptionCount() >
            numExceptionsBefore) {
            // exception reported to bindingContainer during execution
            // TODO: make this behavior configurable (looking for binding errors)
            final DCBindingContainer bindings =
                              (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
            final List exceptions = bindings.getExceptionsList();
            final Throwable lastException = (Throwable) exceptions.get(exceptions.size() - 1);
            throw new AbortProcessingException("exception reported to binding container", lastException);
        }
    }

    private boolean executeOneArgExpression(final FacesEvent event) {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try {
            final MethodExpression expression = (MethodExpression) getFacesBean().getProperty(ONEARG_METHOD_KEY);
            logger.fine("executing one argument " + expression.getExpressionString());
            expression.invoke(elContext, new Object[] { event });
            return true;
        } catch (MethodNotFoundException e) {
            return false;
        }
    }

    private boolean executeNoArgExpression() {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try {
            final MethodExpression expression = (MethodExpression) getFacesBean().getProperty(NOARG_METHOD_KEY);
            logger.fine("executing no-argument " + expression.getExpressionString());
            expression.invoke(elContext, new Object[] { });
            return true;
        } catch (MethodNotFoundException e) {
            return false;
        }
    }

    private int getBindingContainerExceptionCount() {
        final DCBindingContainer bindings = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
        if (bindings == null) {
            return 0;
        }
        List exceptions = bindings.getExceptionsList();
        return exceptions == null ? 0 : exceptions.size();
    }

    /**
     * Attempts to find an {@link BasePolytypeListener.EventType} that matches the
     * {@link BasePolytypeListener.EventType#getMnemonic}.
     * @param mnemonic stylized event name
     * @return matching enumeration or <code>null</code>
     */
    public static BasePolytypeListener.EventType findEventType(String mnemonic) {
        if (mnemonic == null) {
            throw new IllegalArgumentException("mnemonic cannot be null");
        }
        for (BasePolytypeListener.EventType t : BasePolytypeListener.EventType.values()) {
            if (t.getMnemonic().equals(mnemonic)) {
                return t;
            }
        }
        return null;
    }

}
