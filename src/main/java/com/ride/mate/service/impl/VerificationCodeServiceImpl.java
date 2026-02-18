package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.VerificationCodeRepository;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.VerifyCodeRequest;
import com.ride.mate.service.VerificationCodeService;
import com.ride.mate.util.DateUtil;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
@Service
@Transactional(rollbackFor = Exception.class)
public class VerificationCodeServiceImpl extends MessagePropertyBase implements VerificationCodeService {

    private static final int CODE_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final Environment environment;

    public VerificationCodeServiceImpl(VerificationCodeRepository verificationCodeRepository,
                                      JavaMailSender mailSender,
                                      Environment environment) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
        this.environment = environment;
    }

    @Override
    public VerificationCode sendVerificationCode(SendVerificationCodeRequest request) {
        String email = request.getEmail();

        // Delete any existing verification code for this email
        verificationCodeRepository.deleteByEmail(email);

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
    public void verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);
        if (optionalVerificationCode.isEmpty()) {
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_CODE_NOT_FOUND), VERIFICATION_CODE);
        }
        VerificationCode verificationCode = optionalVerificationCode.get();
        // Check if already verified
        if (verificationCode.getVerified().equals(YesNo.YES)) {
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_ALREADY_VERIFIED), VERIFICATION_CODE);
        }
        // Check if code has expired
        if (LocalDateTime.now().isAfter(verificationCode.getExpiryTime())) {
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_CODE_EXPIRED), VERIFICATION_CODE);
        }
        // Check if max attempts exceeded
        if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_MAX_ATTEMPTS_EXCEEDED), VERIFICATION_CODE);
        }
        // Increment attempt count
        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);
        // Verify code
        if (!verificationCode.getCode().equals(code)) {
            verificationCodeRepository.save(verificationCode);
            throw new ValidateRecordException(environment.getProperty(VERIFICATION_INVALID_CODE), CODE);
        }
        // Code is valid - mark as verified
        verificationCode.setVerified(YesNo.YES);
        verificationCode.setModifiedDate(DateUtil.getDate());
        verificationCode.setModifiedUser("SYSTEM");
        verificationCode.setSyncTs(DateUtil.getDate());
        verificationCodeRepository.save(verificationCode);
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
     *
     * @param toEmail recipient email address
     * @param code verification code
     */
    private void sendEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Verification Code - RideMate");
        message.setText("Your verification code is: " + code + "\n\n" +
                "This code will expire in " + CODE_EXPIRY_MINUTES + " minutes.\n\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Best regards,\nRideMate ");
        mailSender.send(message);
    }
}

