package com.paradise_seeker.game.interaction;

public class InteractionState {
    private boolean showInteractHint;
    private String interactText = "> Press F to interact";
    private String interactionType = "";

    public boolean isShowInteractHint() {
        return showInteractHint;
    }

    public void setShowInteractHint(boolean showInteractHint) {
        this.showInteractHint = showInteractHint;
    }

    public String getInteractText() {
        return interactText;
    }

    public void setInteractText(String interactText) {
        this.interactText = interactText;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public void clear() {
        showInteractHint = false;
        interactText = "> Press F to interact";
        interactionType = "";
    }
}