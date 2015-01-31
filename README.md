# adf-method-listener

This JSF/ADF component allows for multiple listeners on a component that each execute a MethodExpression. An example is the easiest way to understand this:
```xml
<af:button text="Multi Action" id="b1">
  <rh:methodExpressionListener method="#{backingBeanScope.myBean.methodOne}"   type="action"/>
  <rh:methodExpressionListener method="#{backingBeanScope.myBean.methodTwo}"   type="action"/>
  <rh:methodExpressionListener method="#{backingBeanScope.myBean.methodThree}" type="action"/>
</af:button>
```

It is similar to the built-in af:setPropertyListener or any other of the ADF listeners. The key difference is that this methodExpressionListener executes an EL method expression that could point to managed beans or operation bindings from the page definition. This makes it also possible to execute multiple ADFm actions:
```xml
<af:button text="Multi ADFm" id="b2">
  <!-- create new rol in a datacontrol collection -->
  <rh:methodExpressionListener method="#{bindings.Create.execute}"      type="action"/>
  <!-- navigate to next row in a datacontrol collection -->
  <rh:methodExpressionListener method="#{bindings.Next.execute}"        type="action"/>
  <!-- execute method exposed on a datacontrol object -->
  <rh:methodExpressionListener method="#{bindings.doSomething.execute}" type="action"/>
</af:button>
```

The methodExpressionListener can also be combined with the build-in ADF listeners:
```xml
<af:button text="Multi Action" id="b1">
  <af:setPropertyListener from="xxx" to="#{backingBeanScope.myBean.someProperty}" type="action"/>
  <rh:methodExpressionListener method="#{backingBeanScope.myBean.methodOne}"      type="action"/>
</af:button>
```

Since MethodExpressionListener extends from [oracle.adf.view.rich.event.BasePolytypeListener](https://docs.oracle.com/middleware/1213/adf/api-reference-faces/oracle/adf/view/rich/event/BasePolytypeListener.html) it supports all of the action types supported by its base class. This includes action, calendar, calendarActivity, calendarActivityDurationChange, calendarDisplayChange, contextInfo, dialog, disclosure, focus, item, launch, launchPopup, poll, popupFetch,  query, queryOperation, rangeChange, regionNavigation, return, returnPopupData, returnPopup, rowDisclosure, selection, sort, and valueChange:
```xml
<af:popup id="p1">
  <rh:methodExpressionListener method="#{backingBeanScope.myBean.popupFetched}" type="popupFetch"/>
  <af:dialog id="d2" title="Dialog">
    <rh:methodExpressionListener method="#{backingBeanScope.myBean.dialogListenerOne}"   type="dialog"/>
    <rh:methodExpressionListener method="#{backingBeanScope.myOtherBean.dialogListener}" type="dialog"/>
    <af:outputText value="Some text" id="ot1"/>
  </af:dialog>
</af:popup>
```
Basically for `type=anyEvent` to work the JSF component the listener is trying to attach to has to have a `public void addAnyEventListener(AnyEventListener)` method. This is also a way to determine the appropriate value for the `type` attribute of the `rh:methodExpressionListener` tag. If the JSF component has a `public void addSomeOtherEventListener(SomeOtherEvent)` method, you know to use `type=someOther`

The expression from the `method` attribute has to point to a method accepting a single appropriate FacesEvent subclass or to a method accepting no argument as all, such as with `#{bindings.Create.execute}`
