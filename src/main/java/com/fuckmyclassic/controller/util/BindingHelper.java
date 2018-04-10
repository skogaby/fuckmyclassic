package com.fuckmyclassic.controller.util;

import javafx.beans.binding.Binding;
import javafx.beans.property.Property;

/**
 * Simple class to cleanly bind properties to UI elements while ensuring
 * that the old properties are unbound.
 * @author skogaby (skogabyskogaby@gmail.com)
 */
public class BindingHelper {

    /**
     * Bind a property unidirectionally.
     * @param src
     * @param dst
     */
    public static void bindProperty(final Property src, final Property dst) {
        dst.unbind();
        dst.bind(src);
    }

    /**
     * Bind a property unidirectionally.
     * @param src
     * @param dst
     */
    public static void bindProperty(final Binding src, final Property dst) {
        dst.unbind();
        dst.bind(src);
    }

    /**
     * Bind a property bidirectionally.
     * @param origProp
     * @param newProp
     * @param dst
     */
    public static void bindPropertyBidirectional(final Property origProp, final Property newProp, final Property dst) {
        if (origProp != null) {
            dst.unbindBidirectional(origProp);
        }

        dst.bindBidirectional(newProp);
    }
}
