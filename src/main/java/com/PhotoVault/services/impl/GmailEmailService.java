package com.PhotoVault.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.PhotoVault.services.EmailService;

@Service
public class GmailEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailService.class);
    private final JavaMailSender mailSender;

    public GmailEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Recovery - PhotoVault");
            message.setText(buildEmailTemplate(resetLink));
            
            // Opcional: Você pode definir um remetente customizado se não quiser usar o padrão configurado
            // message.setFrom("suporte@photovault.com"); 

            mailSender.send(message);
            logger.info("Recovery email successfully sent via Gmail to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send email via Gmail to: {}", to, e);
        }
        
    }

    private String buildEmailTemplate(String resetLink){
        return """
                Hello,

                We received a request to reset your PhotoVault account password.

                Click the secure link below to create a new password:

                %s

                This link will expire in 15 minutes.

                If you did not request this change, no action is required and your current password will remain secure.

                Sincerely,

                The PhotoVault Team
                """.formatted(resetLink);
    }

}
