package com.redheap.methodlistener;

import javax.el.MethodExpression;

import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.event.BasePolytypeListener;

import org.apache.myfaces.trinidad.bean.FacesBean;
import org.apache.myfaces.trinidad.bean.FacesBeanImpl;
import org.apache.myfaces.trinidad.bean.PropertyKey;

public class MethodExpressionListener extends BasePolytypeListener {

    public static final FacesBean.Type TYPE = new FacesBean.Type();
    public static final PropertyKey METHOD_KEY = TYPE.registerKey("method", MethodExpression.class);

    private static final ADFLogger logger = ADFLogger.createADFLogger(MethodExpressionListener.class);

    // Listener state holder with support for saveState and restoreState
    private FacesBean bean;

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
    public void setMethodExpression(MethodExpression method) {
        getFacesBean().setProperty(METHOD_KEY, method);
    }

    /**
     * Gets the "from" or "to" property as a value expression.
     *
     * @param name "from" or "to"
     * @throws IllegalArgumentException name is invalid for this state holder; supports 'from' or 'to'
     */
    public MethodExpression getMethodExpression() {
        return (MethodExpression) getFacesBean().getProperty(METHOD_KEY);
    }

    /**
     * Invoked from the super class with the event matches the target {@link BasePolytypeListener.EventType}.
     * Delegates event handling to the methodExpression supplied to {@link #setMethodExpression}
     * @param event target faces event
     */
    @Override
    protected void handleEvent(FacesEvent event) {
        final MethodExpression expression = getMethodExpression();
        try {
            expression.invoke(FacesContext.getCurrentInstance().getELContext(), new Object[] { event });
        } catch (RuntimeException e) {
            logger.warning("error invoking ActionListener " + expression.getExpressionString(), e);
            throw e;
        }
    }

    /**
     * @return the state holder for the stateful listener
     */
    @Override
    protected FacesBean getFacesBean() {
        if (bean == null) {
            bean = new FacesBeanImpl() {
                @Override
                public FacesBean.Type getType() {
                    return TYPE;
                }
            };
        }
        return bean;
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
