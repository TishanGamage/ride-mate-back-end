package com.ride.mate.core;

/**
 * Message Property Base
 * Base class containing message property constants
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
public abstract class MessagePropertyBase {

    // Field names for validation
    protected static final String VERIFICATION_CODE = "verificationCode";
    protected static final String CODE = "code";

    // Verification message property keys
    protected static final String VERIFICATION_CODE_NOT_FOUND = "verification.code-not-found";
    protected static final String VERIFICATION_ALREADY_VERIFIED = "verification.already-verified";
    protected static final String VERIFICATION_CODE_EXPIRED = "verification.code-expired";
    protected static final String VERIFICATION_MAX_ATTEMPTS_EXCEEDED = "verification.max-attempts-exceeded";
    protected static final String VERIFICATION_INVALID_CODE = "verification.invalid-code";
}
