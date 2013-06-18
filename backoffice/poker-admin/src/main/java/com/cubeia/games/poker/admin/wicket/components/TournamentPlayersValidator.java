package com.cubeia.games.poker.admin.wicket.components;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.util.lang.Objects;


public class TournamentPlayersValidator extends AbstractFormValidator {


    private static final long serialVersionUID = 1L;

    private final FormComponent<Integer>[] components;


    public TournamentPlayersValidator(FormComponent<Integer> minPlayers, FormComponent<Integer> maxPlayers) {
        components = new FormComponent[] { minPlayers, maxPlayers };
    }

    /**
     * @see org.apache.wicket.markup.html.form.validation.IFormValidator#getDependentFormComponents()
     */
    @Override
    public FormComponent<?>[] getDependentFormComponents()
    {
        return components;
    }

    /**
     * @see org.apache.wicket.markup.html.form.validation.IFormValidator#validate(org.apache.wicket.markup.html.form.Form)
     */
    @Override
    public void validate(Form<?> form)
    {
        // we have a choice to validate the type converted values or the raw
        // input values, we validate the raw input
        final FormComponent<Integer> minPlayers = components[0];
        final FormComponent<Integer> maxPlayers = components[1];

        if(maxPlayers.getConvertedInput()<minPlayers.getConvertedInput()) {
            error(maxPlayers);
        }
    }
}
