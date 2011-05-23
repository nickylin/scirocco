package com.android.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

class HttpAuthenticator extends Authenticator {
    private String username;
    private String password;
    public HttpAuthenticator(String username, String password){
        this.username = username;
        this.password = password;
    }
    protected PasswordAuthentication getPasswordAuthentication(){
        return new
            PasswordAuthentication(username, password.toCharArray());
    }
    public String myGetRequestingPrompt(){
        return super.getRequestingPrompt();
    }
}


