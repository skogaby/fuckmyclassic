package com.fuckmyclassic.ui.util;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.util.StringConverter;

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
    public static void bindProperty(final ReadOnlyProperty<?> src, final Property dst) {
        dst.unbind();
        dst.bind(src);
    }

    /**
     * Bind a property unidirectionally.
     * @param src
     * @param dst
     */
    public static void bindProperty(final StringExpression src, final Property dst) {
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
