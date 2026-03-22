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
 * 3 18-03-2026    N/A          N/A          Tishan          Added FILE_SIZE_EXCEEDED constant
 * 4 18-03-2026    N/A          N/A          Tishan          Added scheduler rate update constants
 * 5 18-03-2026    N/A          N/A          Danushka          Added Payment and Withdrawal constants
 * 6 19-03-2026    N/A          N/A          Danushka          Removed PayHere config constants (moved to @Value injection)
 * 7 19-03-2026    N/A          N/A          Tishan            Added Supabase configuration property key constants
 * 8 20-03-2026    N/A          N/A          Danushka            Added Driver Wallet message constants
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

    // User Profile message property keys
    protected static final String USER_PROFILE_ALREADY_EXISTS = "user.profile.already.exists";
    protected static final String PROFILE_PHOTO_UPDATED = "profile.photo.updated";
    protected static final String PROFILE_PHOTO_NOT_FOUND = "profile.photo.not.found";

    // Identification message property keys
    protected static final String IDENTIFICATION_TYPE_NOT_FOUND = "identification.type.not.found";

    // Driver Profile message property keys
    protected static final String DRIVER_PROFILE_NOT_FOUND = "driver.profile.not.found";
    protected static final String DRIVER_PROFILE_ALREADY_EXISTS = "driver.profile.already.exists";
    protected static final String VEHICLE_TYPE_NOT_FOUND = "vehicle.type.not.found";
    protected static final String VEHICLE_MAKE_NOT_FOUND = "vehicle.make.not.found";

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
    protected static final String FILE_SIZE_EXCEEDED = "file.size.exceeded";
    protected static final String DOCUMENT_NOT_FOUND = "document.not.found";
    protected static final String FILE_READ_ERROR = "file.read.error";

    // Supabase storage message property keys
    protected static final String SUPABASE_UPLOAD_FAILED = "supabase.upload.failed";
    protected static final String SUPABASE_DELETE_FAILED = "supabase.delete.failed";
    protected static final String SUPABASE_DOWNLOAD_FAILED = "supabase.download.failed";

    // Reset password message property keys
    protected static final String RESET_PASSWORD_SUCCESS = "reset.password.success";
    protected static final String VERIFICATION_NOT_COMPLETED = "verification.not.completed";

    // Role update message property keys
    protected static final String ROLE_UPDATE_SUCCESS = "role.update.success";
    protected static final String ROLE_INVALID = "role.invalid";

    // Scheduler message property keys
    protected static final String SCHEDULER_RATE_UPDATE_SUCCESS = "scheduler.rate.update.success";
    protected static final String SCHEDULER_RATE_UPDATE_FAILED = "scheduler.rate.update.failed";

    // Payment message property keys
    protected static final String SAVED_CARD_NOT_FOUND = "saved.card.not.found";
    protected static final String PAYMENT_SIGNATURE_INVALID = "payment.signature.invalid";
    protected static final String PAYMENT_CHARGE_FAILED = "payment.charge.failed";
    protected static final String PAYMENT_ORDER_ID_DUPLICATE = "payment.order.id.duplicate";
    protected static final String WITHDRAWAL_INSUFFICIENT_BALANCE = "withdrawal.insufficient.balance";
    protected static final String WITHDRAWAL_REQUEST_NOT_FOUND = "withdrawal.request.not.found";
    protected static final String DRIVER_EARNING_NOT_FOUND = "driver.earning.not.found";

    // Ride price calculation message property keys
    protected static final String DRIVER_VEHICLE_NOT_FOUND = "driver.vehicle.not.found";
    protected static final String VEHICLE_TYPE_RATE_NOT_CONFIGURED = "vehicle.type.rate.not.configured";
    protected static final String RIDE_PRICE_CALCULATED = "ride.price.calculated";

    // Cost splitting message property keys
    protected static final String RIDE_DETAIL_NOT_FOUND = "ride.detail.not.found";
    protected static final String COST_SPLIT_CALCULATED = "cost.split.calculated";
    protected static final String NO_AVAILABLE_SEATS = "no.available.seats";
    protected static final String PASSENGER_ALREADY_JOINED = "passenger.already.joined";

    // Ride detail validation message property keys
    protected static final String AVAILABLE_SEATS_EXCEEDS_VEHICLE_CAPACITY = "available.seats.exceeds.vehicle.capacity";

    // Passenger ride confirm message property keys
    protected static final String RIDE_NOT_FOUND = "ride.not.found";
    protected static final String RIDE_NOT_AVAILABLE = "ride.not.available";
    protected static final String PASSENGER_ALREADY_CONFIRMED = "passenger.already.confirmed";
    protected static final String RIDE_NO_SEATS_AVAILABLE = "ride.no.seats.available";

    protected static final String ACTIVE_RIDE_EXISTS = "active.ride.exists";

    // Ride request message property keys
    protected static final String RIDE_REQUEST_NOT_FOUND = "ride.request.not.found";
    protected static final String RIDE_REQUEST_ALREADY_PENDING = "ride.request.already.pending";
    protected static final String RIDE_REQUEST_ALREADY_PROCESSED = "ride.request.already.processed";
    protected static final String RIDE_REQUEST_CREATED = "ride.request.created";
    protected static final String RIDE_REQUEST_ACCEPTED = "ride.request.accepted";
    protected static final String RIDE_REQUEST_REJECTED = "ride.request.rejected";
    protected static final String RIDE_REQUEST_CANCELLED = "ride.request.cancelled";

    // Driver Wallet message property keys
    protected static final String WALLET_NOT_FOUND = "wallet.not.found";
    protected static final String WALLET_ALREADY_EXISTS = "wallet.already.exists";
    protected static final String WALLET_INSUFFICIENT_BALANCE = "wallet.insufficient.balance";
    protected static final String WALLET_CREDIT_SUCCESS = "wallet.credit.success";
    protected static final String WALLET_DEBIT_SUCCESS = "wallet.debit.success";
    protected static final String WALLET_TRANSACTION_NOT_FOUND = "wallet.transaction.not.found";
    protected static final String INVALID_COMMISSION_PERCENTAGE = "invalid.commission.percentage";

    // Shared Ride message property keys
    protected static final String SHARED_RIDE_CREATED = "shared.ride.created";
    protected static final String SHARED_RIDE_CONFIRMED = "shared.ride.confirmed";
    protected static final String SHARED_RIDE_CANCELLED = "shared.ride.cancelled";
    protected static final String SHARED_RIDE_NOT_FOUND = "shared.ride.not.found";
    protected static final String SHARED_RIDE_COMPLETED = "shared.ride.completed";
    protected static final String RIDE_NOT_ACTIVE = "ride.not.active";
    protected static final String USER_NOT_FOUND = "user.not.found";

    // User Report message property keys
    protected static final String USER_REPORT_NOT_FOUND = "user.report.not.found";
    protected static final String USER_REPORT_SUBMITTED = "user.report.submitted";

    // User Feedback message property keys
    protected static final String USER_FEEDBACK_NOT_FOUND = "user.feedback.not.found";
    protected static final String USER_FEEDBACK_SUBMITTED = "user.feedback.submitted";
}
