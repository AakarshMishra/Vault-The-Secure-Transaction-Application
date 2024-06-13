package com.example.vault;

public class HelperClass {

    String name, email, username, phone, password;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HelperClass(String name, String email, String username, String phone, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.phone = phone;
        this.password = password;
    }

    public HelperClass() {
    }
}
