package com.online.library.domain.enums;

public enum ReservationStatus {
    OCZEKUJĄCA("POTWIERDZONA"),
    POTWIERDZONA("WYPOŻYCZONA"),
    WYPOŻYCZONA("ZWRÓCONA"),
    ZWRÓCONA(null);

    private final String nextState;

    ReservationStatus(String nextState) {
        this.nextState = nextState;
    }

    public boolean canTransitionTo(ReservationStatus target) {
        return nextState != null && nextState.equals(target.name());
    }

    public ReservationStatus getNextState() {
        return nextState != null ? ReservationStatus.valueOf(nextState) : null;
    }

    public boolean hasNextState() {
        return nextState != null;
    }

    public boolean canBeCancelled() {
        return this == OCZEKUJĄCA || this == POTWIERDZONA;
    }

    public boolean isActive() {
        return this == OCZEKUJĄCA || this == POTWIERDZONA || this == WYPOŻYCZONA;
    }
}
