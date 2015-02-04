package com.redheap.methodlistener;

import javax.el.ELException;
import javax.el.MethodExpression;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

import oracle.adf.view.rich.event.BasePolytypeListener;

/**
 * Facelets tag handler for rh:methodExpressionListener that will create a MethodExpressionListener that is attached to
 * the parent JSF component.
 * @see MethodExpressionListener
 */
public class MethodExpressionListenerHandler extends TagHandler {

    // facelet tag attribute values
    private final TagAttribute method;
    private final TagAttribute type;

    /**
     * Default constructor.
     * @param tagConfig Defines the document definition of this handler we are instantiating
     */
    public MethodExpressionListenerHandler(TagConfig tagConfig) {
        super(tagConfig);
        method = getRequiredAttribute("method");
        type = getRequiredAttribute("type");
    }

    /**
     * Create the MethodExpressionListener and attach it to the parent JSF component.
     * @param faceletContext FaceletContext
     * @param parent parent JSF component to attach the listener to
     * @throws FaceletException
     * @throws ELException
     */
    @Override
    public void apply(FaceletContext faceletContext, UIComponent parent) throws FaceletException, ELException {
        // only run on the first time the tag executes
        if (!javax.faces.view.facelets.ComponentHandler.isNew(parent)) {
            return;
        }
        if (parent == null) {
            throw new FaceletException("MethodExpressionListener must be inside UIComponent tag");
        }
        // try to determine type (aka. when to fire)
        BasePolytypeListener.EventType eventType = findType();

        // build MethodExpression from "method" tag attribute. We don't care about return value and accept a single
        // argument that has to be the type-specific subclass of FacesEvent
        MethodExpression singleArgExpression = method.getMethodExpression(faceletContext, null, new Class[] {
                                                                          eventType.getEventClass() });
        MethodExpression noArgExpression = method.getMethodExpression(faceletContext, null, new Class[] { });

        // create the listener and add it to the parent component
        MethodExpressionListener listener = new MethodExpressionListener(eventType);
        listener.setSingleArgumentMethodExpression(singleArgExpression);
        listener.setNoArgumentMethodExpression(noArgExpression);
        if (!MethodExpressionListener.addListener(listener, parent)) {
            throw new FaceletException("Failed to add listener for type " + eventType + " to parent " + parent);
        }
    }

    /**
     * Find the BasePolytypeListener.EventType matching the mnemonic used in the {@code type} attribute of this
     * JSF facelet tag.
     * @return EventType
     * @throws FaceletException if type attribute not set or not a valid value
     * @see BasePolytypeListener.EventType
     */
    private BasePolytypeListener.EventType findType() {
        String typeName = this.type.getValue();
        BasePolytypeListener.EventType eventType = MethodExpressionListener.findEventType(typeName);
        if (eventType == null) {
            // the type is not valid, throw exception
            throw new FaceletException("Unable to find FacesEvent for type attribute " + typeName);
        }
        return eventType;
    }

}
