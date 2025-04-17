package project.home.automation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private final JavaMailSender mailSender;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final Map<String, OtpData> otpCache = new HashMap<>();

    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;

        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(2);

        otpCache.put(email, new OtpData(otp, expiryTime));

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set proper from with name
            helper.setFrom(String.format("Home Automation <%s>", fromEmail));
            helper.setTo(email);
            helper.setSubject("Home Automation - Your OTP Code");

            // Plain text version
            String textContent = String.format(
                    "Welcome to Home Automation!\n\nYour OTP is: %s\nThis OTP is valid for 2 minutes.\nDo not share this OTP with anyone.",
                    otp
            );

            // HTML version
            String htmlContent = "<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>"
                    + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #333;'>Welcome to Home Automation!</h2>"
                    + "<p style='font-size: 16px; color: #555;'>Your OTP is:</p>"
                    + "<p style='font-size: 24px; font-weight: bold; color: #000;'>" + otp + "</p>"
                    + "<p style='font-size: 14px; color: #777;'>This OTP is valid for <b>2 minutes</b>.</p>"
                    + "<p style='font-size: 14px; color: #777;'>Please do not share this OTP with anyone.</p>"
                    + "<hr style='margin: 20px 0;'>"
                    + "<p style='font-size: 12px; color: #aaa;'>If you did not request this, please ignore this email.</p>"
                    + "</div></body></html>";

            helper.setText(textContent, htmlContent);

            // Set headers to reduce spam score
            message.addHeader("X-Priority", "1");
            message.addHeader("X-Mailer", "Spring Boot Mail Sender");
            message.addHeader("Return-Path", fromEmail);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    public boolean isOtpValid(String email, String otp) {
        OtpData otpData = otpCache.get(email);
        if (otpData == null || LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpCache.remove(email);
            return false;
        }
        return otp.equals(otpData.otp);
    }

    public void resendOtp(String email) {
        sendOtp(email);
    }
}
