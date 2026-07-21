package com.cjym.videoweb.service;

import com.cjym.videoweb.provider.CustomThreadPoolExecutorProvider;
import jakarta.annotation.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSendService {
    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private CustomThreadPoolExecutorProvider customThreadPoolExecutorProvider;

    /**
     * 发送邮件并配置相关信息
     * @param from:发送者
     * @param recipient:收件者
     * @param ccRecipient:抄送人
     * @param subject:邮件主题
     * @param text:邮件正文
     * */
    public void send(String from, String recipient, String ccRecipient, String subject, String text) {
        customThreadPoolExecutorProvider.execute(new Thread(()->{
            SimpleMailMessage smm = new SimpleMailMessage();
            smm.setFrom(from);
            smm.setTo(recipient);
            smm.setCc(ccRecipient);
            smm.setSubject(subject);
            smm.setText(text);
            javaMailSender.send(smm);
        }));
    }
}
