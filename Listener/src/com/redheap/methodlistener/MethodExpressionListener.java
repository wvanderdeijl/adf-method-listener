package com.redheap.methodlistener;

import java.util.Collections;
import java.util.List;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodNotFoundException;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
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
 * During event execution this listener checks if the number of exceptions reported to the binding layer increases.
 * If so, it will throw an AbortProcessingException to signal JSF to abort processing any other listeners.
 * See oracle.adf.model.binding.DCBindingContainer.getExceptionsList.
 * <p>
 * This code is inspired by oracle.adf.view.rich.event.SetPropertyListener,
 * oracle.adfinternal.view.faces.facelets.rich.SetPropertyListenerHandler and
 * oracle.adfinternal.view.faces.taglib.listener.SetPropertyListenerTag
 */
public class MethodExpressionListener extends BasePolytypeListener {

    // property keys to store properties in internal FacesBean that takes care of state saving and restore
    private static final PropertyKey NOARG_METHOD_KEY = PropertyKey.createPropertyKey("noarg-method");
    private static final PropertyKey ONEARG_METHOD_KEY = PropertyKey.createPropertyKey("onearg-method");

    /**
     * The no-args constructor default type is {@link BasePolytypeListener.EventType#ACTION}.
     * Can be used to programmatically construct a MethodExpressionListener to add to a component.
     */
    public MethodExpressionListener() {
        super(BasePolytypeListener.EventType.ACTION);
    }

    /**
     * Constructor requires a {@link BasePolytypeListener.EventType} that defines what associated event this particular
     * instance handles.
     * @param eventType framework concrete event
     * @throws IllegalArgumentException eventType cannot be null
     */
    public MethodExpressionListener(BasePolytypeListener.EventType eventType) {
        super(eventType);
    }

    /**
     * Set the MethodExpression without arguments to execute when firing the listener.
     * @param expression a MethodExpression that requires no arguments. The return type doesn't matter and could
     * have been set to {@code null} to indicate this.
     * @see #handleEvent
     */
    public void setNoArgumentMethodExpression(MethodExpression expression) {
        getFacesBean().setProperty(NOARG_METHOD_KEY, expression);
    }

    /**
     * Gets the no-argument MethodExpression.
     * @return MethodExpression as set by {@link #setNoArgumentMethodExpression} or {@code null}
     */
    public MethodExpression getNoArgumentMethodExpression() {
        return (MethodExpression) getFacesBean().getProperty(NOARG_METHOD_KEY);
    }

    /**
     * Set the MethodExpression with a single argument to execute when firing the listener.
     * @param expression a MethodExpression that requires a single argument. This argument type has to be equal to
     * the type associated with the {@code type} attribute. For example, {@code type="action"} requires a
     * MethodExpression that accepts a {@code ActionEvent} argument and {@code type="popupFetch"} requires a
     * MethodExpression accepting a single {@code PopupFetchEvent} argument. The return type doesn't matter and could
     * have been set to {@code null} to indicate this.
     * @see #handleEvent
     * @see BasePolytypeListener.EventType#getEventClass
     */
    public void setSingleArgumentMethodExpression(MethodExpression expression) {
        getFacesBean().setProperty(ONEARG_METHOD_KEY, expression);
    }

    /**
     * Gets the single-argument MethodExpression.
     * @return MethodExpression as set by {@link #setSingleArgumentMethodExpression} or {@code null}
     */
    public MethodExpression getSingleArgumentMethodExpression() {
        return (MethodExpression) getFacesBean().getProperty(ONEARG_METHOD_KEY);
    }

    /**
     * Invoked from the super class with the event matches the target {@link BasePolytypeListener.EventType}.
     * Delegates event handling to the methodExpression supplied to {@link #setSingleArgumentMethodExpression}.
     * If that expression is not given or does not resolve to an existing method, this will retry to delegate
     * to the no-argument method supplied to {@link #setNoArgumentMethodExpression}. This means the same expression
     * can be supplied to both setters if the caller does not know to which type of method the expression will
     * resolve. This handleEvent method will simply try to execute both.
     * @param event target faces event
     * @throws AbortProcessingException
     */
    @Override
    protected void handleEvent(final FacesEvent event) throws AbortProcessingException {
        int numExceptionsBefore = getBindingContainerExceptions().size();
        final MethodExpression singleArgExpr = getSingleArgumentMethodExpression();
        final MethodExpression noArgExpr = getNoArgumentMethodExpression();
        if (singleArgExpr == null && noArgExpr == null) {
            throw new IllegalArgumentException("rf:methodExpressionListner method attribute unknown");
        }
        if (!execute(singleArgExpr, event)) {
            // methodExpression doesn't seem to be for a single-arg method, retry no-arg version
            if (!execute(noArgExpr)) {
                // both expressions null or not resolving to existing methods
                throw new MethodNotFoundException(buildExceptionMessage(singleArgExpr, noArgExpr));
            }
        }
        List postExceptions = getBindingContainerExceptions();
        if (postExceptions.size() > numExceptionsBefore) {
            // exception reported to bindingContainer during execution
            // TODO: make this behavior configurable (looking for binding errors)
            final Throwable lastException = (Throwable) postExceptions.get(postExceptions.size() - 1);
            throw new AbortProcessingException("exception reported to binding container", lastException);
        }
    }

    /**
     * Execute a given MethodExpression.
     * @param expression MethodExpression to execute
     * @param args arguments to supply to the executing methodexpression
     * @return {@code false} when given expression is null or when it throws a MethodNotFoundException, {@code true}
     * if execution succeeded
     * @throws RuntimeException when any other exception than MethodNotFoundException was thrown by the methodexpression
     */
    private boolean execute(final MethodExpression expression, Object... args) {
        final ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        try {
            if (expression == null) {
                return false;
            }
            expression.invoke(elContext, args);
            return true;
        } catch (MethodNotFoundException e) {
            return false;
        }
    }

    /**
     * Build message for exception to throw when both single-argument and no-argument methodExpression were null
     * or pointed to non-existing method (aks threw MethodNotFoundException)
     * @param singleArgExpr single-argument MethodExpression which could be {@code null}
     * @param noArgExpr no-argument MethodExpression which could be {@code null}
     * @return error message explaining which of the two (or both) resolved to non-existing methods or empty
     * string if both methodExpressions were null.
     */
    private String buildExceptionMessage(final MethodExpression singleArgExpr, final MethodExpression noArgExpr) {
        StringBuilder msg = new StringBuilder();
        if (singleArgExpr != null) {
            msg.append(singleArgExpr.getExpressionString()).append(" not resolving to single ");
            msg.append(getEventType().getEventClass().getName()).append(" argument method");
        }
        if (noArgExpr != null) {
            msg.append(msg.length() > 0 ? " and " : "");
            msg.append(noArgExpr.getExpressionString()).append(" not resolving to no-argument method");
        }
        return msg.toString();
    }

    /**
     * Get the list of exceptions for the binding container.
     * @return list of exceptions, which could be empty, but never {@code null}
     */
    private List<?> getBindingContainerExceptions() {
        final DCBindingContainer bindings = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
        if (bindings == null) {
            return Collections.emptyList();
        }
        List exceptions = bindings.getExceptionsList();
        return exceptions == null ? Collections.emptyList() : exceptions;
    }

    /**
     * Attempts to find an {@link BasePolytypeListener.EventType} that matches the
     * {@link BasePolytypeListener.EventType#getMnemonic}.
     * @param mnemonic stylized event name, for exampele {@code action} or {@code popupFetch}
     * @return matching enum or {@code null}
     */
    public static BasePolytypeListener.EventType findEventType(String mnemonic) {
        if (mnemonic == null) {
            return null;
        }
        for (BasePolytypeListener.EventType t : BasePolytypeListener.EventType.values()) {
            if (t.getMnemonic().equals(mnemonic)) {
                return t;
            }
        }
        return null;
    }

}
