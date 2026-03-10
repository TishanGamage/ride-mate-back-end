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
 * 2 09-03-2026    N/A          N/A          Tishan          Added JWT authentication constants
 */
public abstract class MessagePropertyBase {

    // Field names for validation
    protected static final String VERIFICATION_CODE = "verificationCode";
    protected static final String CODE = "code";
    protected static final String SYSTEM = "SYSTEM";

    // Verification message property keys
    protected static final String VERIFICATION_CODE_NOT_FOUND = "verification.code-not-found";
    protected static final String VERIFICATION_ALREADY_VERIFIED = "verification.already-verified";
    protected static final String VERIFICATION_CODE_EXPIRED = "verification.code-expired";
    protected static final String VERIFICATION_MAX_ATTEMPTS_EXCEEDED = "verification.max-attempts-exceeded";
    protected static final String VERIFICATION_INVALID_CODE = "verification.invalid-code";
    protected static final String VERIFICATION_SUCCESS = "verification.success";
    protected static final String VERIFICATION_CODE_SENT_SUCCESS = "verification.code.sent.success";
    protected static final String VERIFICATION_CODE_SENT_FAILED = "verification.code.sent.failed";
    protected static final String RECORD_CREATED = "record.created";
    protected static final String RECORD_UPDATED = "record.updated";
    protected static final String EMAIL_ALREADY_EXISTS = "email.already.exists";
    protected static final String PHONE_NUMBER_ALREADY_EXISTS = "phone.number.already.exists";
    protected static final String EXPIRY_DATE_MUST_BE_FUTURE = "expiry.date.must.be.future";

    // Common message property keys
    protected static final String RECORD_NOT_FOUND = "record.not.found";
    protected static final String RECORD_VERSION_MISMATCH = "record.version.mismatch";

    // Identification message property keys
    protected static final String IDENTIFICATION_TYPE_NOT_FOUND = "identification.type.not.found";

    // Login message property keys
    protected static final String LOGIN_USER_NOT_FOUND = "login.user-not-found";
    protected static final String LOGIN_INVALID_CREDENTIALS = "login.invalid-credentials";
    protected static final String LOGIN_ACCOUNT_SUSPENDED = "login.account-suspended";
    protected static final String LOGIN_EMAIL_NOT_VERIFIED = "login.email-not-verified";
    protected static final String LOGIN_SUCCESS = "login.success";

    // JWT authentication message property keys
    protected static final String JWT_TOKEN_INVALID = "jwt.token.invalid";
    protected static final String JWT_TOKEN_EXPIRED = "jwt.token.expired";
    protected static final String JWT_REFRESH_TOKEN_INVALID = "jwt.refresh.token.invalid";
    protected static final String JWT_REFRESH_TOKEN_EXPIRED = "jwt.refresh.token.expired";
    protected static final String JWT_TOKEN_REFRESHED = "jwt.token.refreshed";
    protected static final String JWT_USER_NOT_FOUND = "jwt.user.not.found";

    // File upload message property keys
    protected static final String FILE_UPLOADED_SUCCESS = "file.uploaded.success";
    protected static final String FILES_UPLOADED_SUCCESS = "files.uploaded.success";
    protected static final String FILE_DELETED_SUCCESS = "file.deleted.success";
    protected static final String FILE_EMPTY = "file.empty";
    protected static final String FILE_NAME_EMPTY = "file.name.empty";
    protected static final String DOCUMENT_NOT_FOUND = "document.not.found";
    protected static final String FILE_READ_ERROR = "file.read.error";

    // Supabase storage message property keys
    protected static final String SUPABASE_UPLOAD_FAILED = "supabase.upload.failed";
    protected static final String SUPABASE_DELETE_FAILED = "supabase.delete.failed";
    protected static final String SUPABASE_DOWNLOAD_FAILED = "supabase.download.failed";
}
