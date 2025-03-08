package cn.techwolf.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("iplatform@kanzhun.com");
        message.setTo(to);
        message.setSubject("验证码");
        message.setText("您的验证码是: " + code + "\n该验证码5分钟内有效，请勿泄露给他人。");

        mailSender.send(message);
    }
}