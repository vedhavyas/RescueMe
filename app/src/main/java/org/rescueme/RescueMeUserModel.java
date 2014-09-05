package org.rescueme;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;

/**
 * Created by vedhavyas.singareddi on 05-09-2014.
 */
public class RescueMeUserModel {

    private String id;
    private String name;
    private String hashPassword;
    private String email;
    private String number;

    public RescueMeUserModel(String name, String password,String email, String number){
        this.name = name;
        this.hashPassword = getHash(password);
        this.email = email;
        this.number = number;
    }

    public RescueMeUserModel(String email, String password){
        this.email = email;
        this.hashPassword = getHash(password);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    private String getHash(String password){
        java.security.MessageDigest d;
        try {
            d = java.security.MessageDigest.getInstance("SHA-1");
            d.reset();
            d.update(password.getBytes());
            return convertToHex(d.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String convertToHex(byte[] data){
        StringBuilder sb = new StringBuilder();
        String hex;

        hex= Base64.encodeToString(data, 0, data.length, Base64.DEFAULT);

        sb.append(hex);

        return sb.toString();
    }
}
