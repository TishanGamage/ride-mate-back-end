package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.repository.VerificationCodeRepository;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.VerifyCodeRequest;
import com.ride.mate.service.VerificationCodeService;
import com.ride.mate.util.DateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Verification Code Service Implementation
 * Implementation of business logic for email verification codes
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@Service
@Transactional
public class VerificationCodeServiceImpl extends MessagePropertyBase implements VerificationCodeService {

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final Environment environment;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VerificationCodeServiceImpl(VerificationCodeRepository verificationCodeRepository,
                                       JavaMailSender mailSender,
                                       Environment environment, UserRepository userRepository) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
        this.environment = environment;
        this.userRepository = userRepository;
    }

    @Override
    public VerificationCode sendVerificationCode(SendVerificationCodeRequest request) {
        String email = request.getEmail();

        verificationCodeRepository.deleteByEmail(email);
        entityManager.flush();

        String code = generateSixDigitCode();

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setTargetEmail(request.getTargetEmail());
        verificationCode.setCode(code);
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCode.setVerified(YesNo.NO);
        verificationCode.setAttemptCount(0L);
        verificationCode.setCreatedDate(DateUtil.getDate());
        verificationCode.setCreatedUser(SYSTEM);
        verificationCode.setSyncTs(DateUtil.getDate());

        verificationCodeRepository.save(verificationCode);
        sendEmail(email, code);

        return verificationCode;
    }

    @Override
    public SuccessAndErrorDetailsResource verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);
        if (optionalVerificationCode.isEmpty()) {
            log.warn("No verification code found for email: {}", email);
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_CODE_NOT_FOUND), Boolean.FALSE);
        }
        VerificationCode verificationCode = optionalVerificationCode.get();

        if (verificationCode.getVerified().equals(YesNo.YES)) {
            log.warn("Email already verified: {}", email);
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_ALREADY_VERIFIED), Boolean.FALSE);
        }
        if (LocalDateTime.now().isAfter(verificationCode.getExpiryTime())) {
            log.warn("Verification code expired for email: {}, expiry: {}", email, verificationCode.getExpiryTime());
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_CODE_EXPIRED), Boolean.FALSE);
        }
        if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
            log.warn("Max attempts exceeded for email: {}, attempts: {}", email, verificationCode.getAttemptCount());
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_MAX_ATTEMPTS_EXCEEDED), Boolean.FALSE);
        }

        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);
        verificationCode.setModifiedDate(DateUtil.getDate());
        verificationCode.setModifiedUser(SYSTEM);
        verificationCode.setSyncTs(DateUtil.getDate());

        if (!verificationCode.getCode().equals(code)) {
            verificationCodeRepository.saveAndFlush(verificationCode);
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_INVALID_CODE), false);
        }

        verificationCode.setVerified(YesNo.YES);
        verificationCodeRepository.saveAndFlush(verificationCode);

        // Resolve the target user:
        // - Step 2 (inbox verify): userId is provided in the request → activate that user
        // - Step 1 (own email verify): no userId → find user by email → mark emailVerified
        boolean isStep2 = request.getUserId() != null;
        Optional<User> optionalUser = isStep2
                ? userRepository.findById(request.getUserId())
                : userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("User not found during verification. isStep2={}, email={}, userId={}", isStep2, email, request.getUserId());
            return new SuccessAndErrorDetailsResource(environment.getProperty(RECORD_NOT_FOUND), Boolean.FALSE);
        }
        User user = optionalUser.get();

        if (isStep2) {
            // Step 2: inbox verified → activate the user
            user.setStatus(com.ride.mate.enums.UserStatus.ACTIVE);
            log.info("Step 2 verified: User {} set to ACTIVE", user.getId());
        } else {
            // Step 1: own email verified
            user.setEmailVerified(YesNo.YES);
            log.info("Step 1 verified: emailVerified set for user {}", user.getId());
        }
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(SYSTEM);
        userRepository.saveAndFlush(user);

        return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_SUCCESS), true);
    }

    /**
     * Generates a random 6-digit verification code
     *
     * @return 6-digit code as string
     */
    private String generateSixDigitCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    public void ensureVerified(String email) {
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(VERIFICATION_CODE_NOT_FOUND), "errorMessage"));
        if (!verificationCode.getVerified().equals(YesNo.YES)) {
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_NOT_COMPLETED), "errorMessage");
        }
    }

    /**
     * Sends email with verification code
     * If email sending fails (e.g., in development), logs the code to console
     *
     * @param toEmail recipient email address
     * @param code verification code
     */
    private void sendEmail(String toEmail, String code) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your RideMate Verification Code");

            // Load HTML template from file
            String htmlContent = loadEmailTemplate();

            // Replace placeholders with actual values
            htmlContent = htmlContent.replace("{{VERIFICATION_CODE}}", code);
            htmlContent = htmlContent.replace("{{EXPIRY_MINUTES}}", String.valueOf(CODE_EXPIRY_MINUTES));

            helper.setText(htmlContent, true);

            // Add inline logo image
            ClassPathResource logoResource = new ClassPathResource("assets/ride-mate-logo-dark.png");
            helper.addInline("logo", logoResource);

            mailSender.send(mimeMessage);

            log.info("Verification code email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", toEmail, e.getMessage());
            log.warn("==========================================================");
            log.warn("EMAIL SENDING FAILED - DEVELOPMENT MODE");
            log.warn("Verification code for {}: {}", toEmail, code);
            log.warn("Code expires in {} minutes", CODE_EXPIRY_MINUTES);
            log.warn("==========================================================");
        }
    }

    /**
     * Loads the email template from resources
     *
     * @return HTML template as string
     * @throws IOException if template file cannot be read
     */
    private String loadEmailTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/verification-email.html");
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

}