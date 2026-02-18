package com.ride.mate.service.impl;

import com.ride.mate.domain.VerificationCode;
import com.ride.mate.repository.VerificationCodeRepository;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.VerifyCodeRequest;
import com.ride.mate.service.VerificationCodeService;
import jakarta.transaction.Transactional;
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
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final int CODE_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;

    public VerificationCodeServiceImpl(VerificationCodeRepository verificationCodeRepository,
                                      JavaMailSender mailSender) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public String sendVerificationCode(SendVerificationCodeRequest request) {
        String email = request.getEmail();

        // Delete any existing verification code for this email
        verificationCodeRepository.deleteByEmail(email);

        // Generate a 6-digit random code
        String code = generateSixDigitCode();

        // Create and save verification code entity
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setCreatedDate(LocalDateTime.now());
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        verificationCode.setVerified(false);
        verificationCode.setAttemptCount(0);

        verificationCodeRepository.save(verificationCode);

        // Send email with verification code
        sendEmail(email, code);

        return "Verification code sent successfully to " + email;
    }

    @Override
    @Transactional
    public boolean verifyCode(VerifyCodeRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        Optional<VerificationCode> optionalVerificationCode = verificationCodeRepository.findByEmail(email);

        if (optionalVerificationCode.isEmpty()) {
            return false;
        }

        VerificationCode verificationCode = optionalVerificationCode.get();

        // Check if already verified
        if (verificationCode.getVerified()) {
            return false;
        }

        // Check if code has expired
        if (LocalDateTime.now().isAfter(verificationCode.getExpiryTime())) {
            return false;
        }

        // Check if max attempts exceeded
        if (verificationCode.getAttemptCount() >= MAX_ATTEMPTS) {
            return false;
        }

        // Increment attempt count
        verificationCode.setAttemptCount(verificationCode.getAttemptCount() + 1);

        // Verify code
        if (verificationCode.getCode().equals(code)) {
            verificationCode.setVerified(true);
            verificationCodeRepository.save(verificationCode);
            return true;
        } else {
            verificationCodeRepository.save(verificationCode);
            return false;
        }
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

