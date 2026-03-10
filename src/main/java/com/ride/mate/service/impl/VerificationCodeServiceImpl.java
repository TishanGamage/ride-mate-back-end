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

        // Delete any existing verification code for this email
        verificationCodeRepository.deleteByEmail(email);
        entityManager.flush();

        // Generate a 6-digit random code
        String code = generateSixDigitCode();

        // Create and save verification code entity
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);

        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCode.setVerified(YesNo.NO);
        verificationCode.setAttemptCount(0L);
        verificationCode.setCreatedDate(DateUtil.getDate());
        verificationCode.setCreatedUser(SYSTEM);
        verificationCode.setSyncTs(DateUtil.getDate());

        verificationCodeRepository.save(verificationCode);

        // Send email with verification code
        sendEmail(email, code);

        return verificationCode;
    }

    @Override
    public SuccessAndErrorDetailsResource verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);
        if (optionalVerificationCode.isEmpty()) {
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_CODE_NOT_FOUND), Boolean.FALSE);
        }
        VerificationCode verificationCode = optionalVerificationCode.get();
        // Check if already verified
        if (verificationCode.getVerified().equals(YesNo.YES)) {
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_ALREADY_VERIFIED), Boolean.FALSE);
        }
        // Check if code has expired
        if (LocalDateTime.now().isAfter(verificationCode.getExpiryTime())) {
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_CODE_EXPIRED), Boolean.FALSE);
        }
        // Check if max attempts exceeded
        if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_MAX_ATTEMPTS_EXCEEDED), Boolean.FALSE);
        }
        // Increment attempt count
        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);
        verificationCode.setModifiedDate(DateUtil.getDate());
        verificationCode.setModifiedUser(SYSTEM);
        verificationCode.setSyncTs(DateUtil.getDate());

        // Verify code
        if (!verificationCode.getCode().equals(code)) {
            verificationCodeRepository.saveAndFlush(verificationCode);
            return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_INVALID_CODE), false);
        }
        // Code is valid - mark as verified
        verificationCode.setVerified(YesNo.YES);
        verificationCodeRepository.saveAndFlush(verificationCode);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));
        user.setEmailVerified(YesNo.YES);
        userRepository.saveAndFlush(user);
        return new SuccessAndErrorDetailsResource(environment.getProperty(VERIFICATION_SUCCESS),true);
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