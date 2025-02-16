/*
 * Copyright 2018: Thomson Reuters. All Rights Reserved. Proprietary and
 * Confidential information of Thomson Reuters. Disclosure, Use or Reproduction
 * without the written authorization of Thomson Reuters is prohibited.
 */
package com.gralll.sam.model;

public class ContactUsRequest {
    private String subject;
    private String username;
    private String phone;
    private String email;
    private String question;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}