package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Override
    @Async
    public void sendBudgetAlertEmail(User user, String categoryName, BigDecimal budgetAmount,
                                     BigDecimal spentAmount, int percentageUsed, String alertType) {
        try {
            String subject = alertType.equals("EXCEEDED") 
                    ? "Budget Alert: You've exceeded your budget!" 
                    : "Budget Warning: Approaching budget limit";

            String content = buildBudgetAlertContent(user, categoryName, budgetAmount, spentAmount, percentageUsed, alertType);
            sendHtmlEmail(user.getEmail(), subject, content);
            log.info("Budget alert email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send budget alert email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    public void send2FACode(String email, String code) {
        try {
            String subject = "Your Expense Manager 2FA Code";
            String content = build2FAContent(code);
            sendHtmlEmail(email, subject, content);
            log.info("2FA code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send 2FA code to {}: {}", email, e.getMessage());
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            String subject = "Welcome to Expense Manager!";
            String content = buildWelcomeContent(user);
            sendHtmlEmail(user.getEmail(), subject, content);
            log.info("Welcome email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }

    private String buildBudgetAlertContent(User user, String categoryName, BigDecimal budgetAmount,
                                         BigDecimal spentAmount, int percentageUsed, String alertType) {
        String emoji = alertType.equals("EXCEEDED") ? "🚨" : "⚠️";
        String color = alertType.equals("EXCEEDED") ? "#dc2626" : "#f59e0b";

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: %s; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                .alert-box { background: white; border-left: 4px solid %s; padding: 20px; margin: 20px 0; border-radius: 4px; }
                .stats { display: flex; justify-content: space-between; margin: 20px 0; }
                .stat { text-align: center; padding: 15px; background: white; border-radius: 8px; }
                .stat-value { font-size: 24px; font-weight: bold; color: %s; }
                .progress-bar { background: #e5e7eb; height: 20px; border-radius: 10px; overflow: hidden; margin: 20px 0; }
                .progress-fill { height: 100%%; background: %s; border-radius: 10px; }
                .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>%s Budget Alert</h1>
                    <p>Hi %s,</p>
                </div>
                <div class="content">
                    <div class="alert-box">
                        <h2 style="margin-top: 0;">%s %s</h2>
                        <p>You have reached <strong>%d%%</strong> of your budget for <strong>%s</strong>.</p>
                    </div>
                    
                    <div class="stats">
                        <div class="stat">
                            <div class="stat-value">$%.2f</div>
                            <div>Budget</div>
                        </div>
                        <div class="stat">
                            <div class="stat-value">$%.2f</div>
                            <div>Spent</div>
                        </div>
                        <div class="stat">
                            <div class="stat-value">$%.2f</div>
                            <div>Remaining</div>
                        </div>
                    </div>
                    
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: %d%%;"></div>
                    </div>
                    
                    <p>Review your spending and consider adjusting your budget if needed.</p>
                    <p style="text-align: center; margin-top: 30px;">
                        <a href="#" style="background: #4f46e5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px;">View Dashboard</a>
                    </p>
                </div>
                <div class="footer">
                    <p>Expense Manager - Manage your finances wisely</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(color, color, color, color, emoji, user.getFullName(),
                emoji, alertType.equals("EXCEEDED") ? "Budget Exceeded!" : "Budget Warning",
                percentageUsed, categoryName, budgetAmount, spentAmount,
                budgetAmount.subtract(spentAmount), Math.min(percentageUsed, 100));
    }

    private String build2FAContent(String code) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 500px; margin: 0 auto; padding: 20px; }
                .header { background: #4f46e5; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9fafb; padding: 40px; text-align: center; border-radius: 0 0 8px 8px; }
                .code { font-size: 48px; font-weight: bold; letter-spacing: 10px; color: #4f46e5; 
                        background: white; padding: 20px 40px; border-radius: 8px; display: inline-block; margin: 20px 0; }
                .warning { font-size: 12px; color: #6b7280; margin-top: 20px; }
                .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Two-Factor Authentication</h1>
                </div>
                <div class="content">
                    <p>Your verification code is:</p>
                    <div class="code">%s</div>
                    <p>This code will expire in 5 minutes.</p>
                    <div class="warning">
                        <p>If you did not request this code, please ignore this email.</p>
                        <p>Do not share this code with anyone.</p>
                    </div>
                </div>
                <div class="footer">
                    <p>Expense Manager</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(code);
    }

    private String buildWelcomeContent(User user) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; padding: 40px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9fafb; padding: 40px; border-radius: 0 0 8px 8px; }
                .feature { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; display: flex; align-items: center; gap: 15px; }
                .feature-icon { font-size: 24px; }
                .cta { text-align: center; margin-top: 30px; }
                .cta a { background: #4f46e5; color: white; padding: 15px 40px; text-decoration: none; border-radius: 6px; display: inline-block; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Welcome to Expense Manager!</h1>
                    <p>Hi %s, thank you for joining us.</p>
                </div>
                <div class="content">
                    <p>You're now ready to take control of your finances. Here's what you can do:</p>
                    
                    <div class="feature">
                        <span class="feature-icon">💰</span>
                        <div><strong>Track Transactions</strong><br/>Log your income and expenses easily</div>
                    </div>
                    <div class="feature">
                        <span class="feature-icon">📊</span>
                        <div><strong>Monitor Spending</strong><br/>View detailed reports and insights</div>
                    </div>
                    <div class="feature">
                        <span class="feature-icon">🎯</span>
                        <div><strong>Set Budgets</strong><br/>Get alerts when approaching limits</div>
                    </div>
                    
                    <div class="cta">
                        <a href="#">Get Started</a>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.formatted(user.getFullName());
    }
}
