package org.rescueme;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Authored by vedhavyas.singareddi on 18-09-2014.
 */
public class RescueMeUtilClass {

    public static byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] blob = null;
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blob = stream.toByteArray();
        }

        return blob;
    }

    public static Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

    public static String isUserDataValid(RescueMeUserModel userModel) {
        if (!isNameEmpty(userModel.getName())) {
            if (validEmail(userModel.getEmail())) {
                if (validNumber(userModel.getNumber())) {
                    if (!isMessageEmpty(userModel.getMessage())) {
                        return RescueMeConstants.SUCCESS;
                    } else {
                        return RescueMeConstants.MESSAGE_EMPTY;
                    }
                } else {
                    return RescueMeConstants.PHONE_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        } else {
            return RescueMeConstants.NAME_EMPTY;
        }

    }

    public static String isContactDataValid(RescueMeUserModel contact) {
        if (!isNameEmpty(contact.getName())) {
            if (validEmail(contact.getEmail())) {
                if (validNumber(contact.getNumber())) {
                    return RescueMeConstants.SUCCESS;
                } else {
                    return RescueMeConstants.PHONE_FAIL;
                }
            } else {
                return RescueMeConstants.EMAIL_FAIL;
            }
        } else {
            return RescueMeConstants.NAME_EMPTY;
        }

    }

    private static boolean validEmail(String email) {
        return email.contains(RescueMeConstants.EMAIL_REGEX);

    }

    private static boolean validNumber(String phoneNumber) {
        return phoneNumber.length() >= RescueMeConstants.PHONE_NUMBER_LENGTH && phoneNumber.matches("^\\+[0-9]+");

    }

    private static boolean isNameEmpty(String name) {
        return name.isEmpty();
    }

    private static boolean isMessageEmpty(String personalMessage) {
        return personalMessage.isEmpty();
    }
}
