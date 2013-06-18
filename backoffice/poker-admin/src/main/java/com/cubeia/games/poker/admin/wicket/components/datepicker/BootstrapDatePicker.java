/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.admin.wicket.components.datepicker;

import com.googlecode.wicket.jquery.ui.IJQueryWidget;
import com.googlecode.wicket.jquery.ui.JQueryBehavior;
import com.googlecode.wicket.jquery.ui.Options;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import java.util.Date;

public class BootstrapDatePicker extends DateTextField implements IJQueryWidget {
    private static final long serialVersionUID = 1L;
    private static final String METHOD = "datepicker";

    private Options options;

    /**
     * Constructor
     *
     * @param id the markup id
     */
    public BootstrapDatePicker(String id) {
        this(id, new Options());
    }

    /**
     * Constructor
     *
     * @param id      the markup id
     * @param options {@link Options}
     */
    public BootstrapDatePicker(String id, Options options) {
        super(id);

        this.options = options;
    }

    /**
     * Constructor
     *
     * @param id      the markup id
     * @param pattern a <code>SimpleDateFormat</code> pattern
     * @param options {@link Options}
     */
    public BootstrapDatePicker(String id, String pattern, Options options) {
        super(id, pattern);

        this.options = options;
    }

    /**
     * Constructor
     *
     * @param id    the markup id
     * @param model the {@link IModel}
     */
    public BootstrapDatePicker(String id, IModel<Date> model) {
        this(id, model, new Options());
    }

    /**
     * Constructor
     *
     * @param id      the markup id
     * @param model   the {@link IModel}
     * @param options {@link Options}
     */
    public BootstrapDatePicker(String id, IModel<Date> model, Options options) {
        super(id, model);

        this.options = options;
    }

    /**
     * Constructor
     *
     * @param id      the markup id
     * @param model   the {@link IModel}
     * @param pattern a <code>SimpleDateFormat</code> pattern
     * @param options {@link Options}
     */
    public BootstrapDatePicker(String id, IModel<Date> model, String pattern, Options options) {
        super(id, model, pattern);

        this.options = options;
    }

    // Events //
    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.add(JQueryWidget.newWidgetBehavior(this));
    }

    /**
     * Called immediately after the onConfigure method in a behavior. Since this is before the rendering
     * cycle has begun, the behavior can modify the configuration of the component (i.e. {@link Options})
     *
     * @param behavior the {@link JQueryBehavior}
     */
    protected void onConfigure(JQueryBehavior behavior) {
        behavior.add(new JavaScriptResourceReference(BootstrapDatePicker.class, "bootstrap-datepicker.js"));
        behavior.add(new CssResourceReference(BootstrapDatePicker.class, "datepicker.css"));
    }

    // IJQueryWidget //
    @Override
    public JQueryBehavior newWidgetBehavior(String selector) {
        return new JQueryBehavior(selector, METHOD, this.options) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onConfigure(Component component) {
                BootstrapDatePicker.this.onConfigure(this);
            }
        };
    }
}