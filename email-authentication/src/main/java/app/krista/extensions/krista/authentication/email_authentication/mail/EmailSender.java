/*
 * Email Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.krista.authentication.email_authentication.mail;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class EmailSender {

    private final ExecutorService executorService;
    private final MailSessionProvider mailSessionProvider;
    private final Queue<EmailWork> emailWorkList = new ConcurrentLinkedQueue<>();

    public EmailSender(MailSessionProvider mailSessionProvider) {
        this(Executors.newSingleThreadExecutor(), mailSessionProvider);
    }

    public EmailSender(ExecutorService executorService, MailSessionProvider mailSessionProvider) {
        this.executorService = executorService;
        this.mailSessionProvider = mailSessionProvider;
    }

    public void init() {
        executorService.submit(new Worker());
    }

    private Message createMessage(String toEmailAddress, String subject, String messageBody) throws MessagingException {
        Message message = new MimeMessage(mailSessionProvider.getSession());
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }

    public void sendMessage(String toEmailAddress, String subject, String messageBody, String secret)
            throws MessagingException {
        emailWorkList.offer(new EmailWork(createMessage(toEmailAddress, subject, messageBody), secret));
    }

    public static class EmailWork {

        private final Message message;
        private final String sessionId;

        public EmailWork(Message message, String sessionId) {
            this.message = message;
            this.sessionId = sessionId;
        }

        public Message getMessage() {
            return message;
        }

        public String getSessionId() {
            return sessionId;
        }

    }

    public class Worker implements Runnable {

        private boolean work = true;

        public void terminate() {
            this.work = false;
        }

        @Override
        public void run() {
            while (work) {
                try {
                    sendMails(emailWorkList.poll());
                } catch (MessagingException cause) {
                    // handle exception
                }
            }
        }

        private void sendMails(EmailWork firstWork) throws MessagingException {
            Session session = mailSessionProvider.getSession();
            try (Transport transport = session.getTransport()) {
                transport.connect();
                for (EmailWork work = firstWork; emailWorkList.peek() != null; work = emailWorkList.poll()) {
                    Message message = work.getMessage();
                    transport.sendMessage(message, message.getAllRecipients());
                    // update key value store on status
                }
            }
        }

    }

}
