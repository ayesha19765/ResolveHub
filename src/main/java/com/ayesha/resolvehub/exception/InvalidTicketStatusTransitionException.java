package com.ayesha.resolvehub.exception;

public class InvalidTicketStatusTransitionException extends RuntimeException {

    public InvalidTicketStatusTransitionException(String message) {
        super(message);
    }

    public InvalidTicketStatusTransitionException(String currentStatus, String targetStatus) {
        super("Cannot transition ticket status from " + currentStatus + " to " + targetStatus);
    }
}
