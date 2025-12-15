package com.kompu.api.entity.subscription.exception;

/**
 * SubscriptionPlanNotFoundException - Thrown when subscription plan is not
 * found
 */
public class SubscriptionPlanNotFoundException extends RuntimeException {

    public SubscriptionPlanNotFoundException(String message) {
        super(message);
    }

    public SubscriptionPlanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SubscriptionPlanNotFoundException() {
        super("Subscription plan not found in the system");
    }
}
