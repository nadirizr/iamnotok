package com.google.iamnotok.senders.email_sender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.util.Log;

import com.google.iamnotok.Contact;
import com.google.iamnotok.LocationAddress;
import com.google.iamnotok.Preferences.VigilanceState;
import com.google.iamnotok.senders.NotificationSender;
import com.google.iamnotok.utils.AccountUtils;
import com.google.iamnotok.utils.FormatUtils;

public class EmailNotificationSender implements NotificationSender {
	
	private static final String LOG = "EmailNotificationSender";

	private final FormatUtils formatUtils;
	private final AccountUtils accountUtils;

	public EmailNotificationSender(FormatUtils formatUtils, AccountUtils accountUtils) {
		this.formatUtils = formatUtils;
		this.accountUtils = accountUtils;
	}

	@Override
	public boolean sendNotifications(
			Collection<Contact> contacts, LocationAddress locationAddress, VigilanceState state) {
		List<String> emailList = getAllContactEmails(contacts);
		if (emailList.size() > 0) {
			sendEmailMessage(emailList, locationAddress, state);
		}

		return true;
	}

	private List<String> getAllContactEmails(Collection<Contact> contacts) {
		List<String> emails = new ArrayList<String>();
		for (Contact contact : contacts) {
			for (String email : contact.getEnabledEmails()) {
				emails.add(email);
			}
		}
		return emails;
	}

	/**
	 * Sends an email
	 */
	private void sendEmailMessage(
			List<String> to, LocationAddress locationAddress, VigilanceState state) {
	  String recipients = formatUtils.formatRecipients(to);
	  Log.d(LOG, "Sending email to: " + to);
	  String subject = formatUtils.formatSubject(accountUtils.getAccountName(), accountUtils.getPhoneNumber());
	  String message = "";
	  if (state == VigilanceState.NORMAL_STATE) {
	    message = "I am OK now";
	    Log.d(LOG, "Sending the email " + message);
	  } else {
	    Log.d(LOG, "Getting location");
	    message = formatUtils.formatMessage(locationAddress, accountUtils.getCustomMessage());
	    if (locationAddress.location != null) {
	      message += " " + getMapUrl(locationAddress);
	    }
	  }

	  try {
	    GMailSender sender = new GMailSender("imnotokandroidapplication@gmail.com", "googlezurich");
	    String mailAddress = accountUtils.getMailAddress();
	    sender.sendMail(mailAddress, subject, message, "imnotokandroidapplication@gmail.com", recipients);
	  } catch (Exception e) {
	    Log.e(LOG, e.getMessage(), e);
	  }
	}

	private String getMapUrl(LocationAddress locAddr) {
		String template = "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=%f,%f&sll=%f,%f&sspn=0.005055,0.009645&ie=UTF8&z=16";
		return String.format(template, locAddr.location.getLatitude(), locAddr.location.getLongitude(),
				locAddr.location.getLatitude(), locAddr.location.getLongitude());
	}
}
