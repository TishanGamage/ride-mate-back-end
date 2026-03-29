/**
 * EmailService
 * Service for sending emails (notifications, ride summaries, etc.)
 *
 * @author Copilot
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 29-03-2026    N/A          N/A          Copilot          Initial Development
 */
package com.ride.mate.service;

import org.springframework.mail.MailException;

public interface EmailService {
    void sendEmail(String to, String subject, String body) throws MailException;
}

