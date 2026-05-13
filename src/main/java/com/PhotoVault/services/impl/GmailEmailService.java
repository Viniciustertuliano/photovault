package com.PhotoVault.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.PhotoVault.services.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class GmailEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(GmailEmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String shareLinkUrl;

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
            
            // message.setFrom("suporte@photovault.com"); 

            mailSender.send(message);
            logger.info("Recovery email successfully sent via Gmail to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send email via Gmail to: {}", to, e);
        }
        
    }

    @Override
    @Async
    public void sendShareLinkEmail(String toEmail, String photographerName, String folderName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your photos are ready! - " + photographerName);

            String accessUrl = shareLinkUrl + "/share/" + token;

            String htmlBody = buildShareLinkHtmlTemplate(photographerName, folderName, accessUrl);

            helper.setText(htmlBody, true);

            mailSender.send(message);
            logger.info("Sharing email successfully sent via Gmail to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Error composing HTML email via Gmail for {}", toEmail, e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email via Gmail to {}", toEmail, e);
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

    private String buildShareLinkHtmlTemplate(String photographerName, String folderName, String accessUrl) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; color: #333; border: 1px solid #e0e0e0; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #2c3e50; text-align: center;">Your photos have arrived!</h2>
                    <p>Hello!</p>
                    <p>The photographer <strong>%s</strong> has just shared the gallery <strong>"%s"</strong> with you.</p>
                    <p>To view, download, and safely keep these memories, click the button below to access your gallery:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3498db; color: white; padding: 14px 28px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                            Access My Photos
                        </a>
                    </div>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #7f8c8d; text-align: justify;">
                        <em>Security note: To protect your privacy and your photos, access to this gallery requires login. If you do not have a PhotoVault account yet, you can quickly create one by clicking the button above.</em>
                    </p>
                </div>
                """.formatted(photographerName, folderName, accessUrl);
    }
    

}
