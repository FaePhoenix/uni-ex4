package fae;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class Mailing {

    private Properties props;
    private Session session;
    
    public Mailing() {

        //Set Properties
        this.props = new Properties();
        this.props.put("mail.smtp.auth", true);
        this.props.put("mail.smtp.starttls.enable", "true");
        this.props.put("mail.smtp.host", "smtp.web.de");
        this.props.put("mail.smtp.port", "587");

        //Get Session
        this.session = Session.getInstance(this.props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("sheyne3005@web.de", "Di1P4W&d*");
            }
        });
    }

    public Boolean sendEmail(String recipient, String mailSubject, String content) {
        try {
            //Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sheyne3005@web.de"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(mailSubject);

            //Fill message
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(content, "text/html; charset=utf-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            //Send message
            Transport.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
