package io.github.jacktown11.test;

import javax.mail.MessagingException;

import org.junit.Test;

import io.github.jacktown11.utils.MailUtils;

public class Test1 {
	
	@Test
	public void sendEmail() {
		try {
			MailUtils.sendMail("lucy@jacktown.com", "<a href='http://localhost:8080'>eshop</a>");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
