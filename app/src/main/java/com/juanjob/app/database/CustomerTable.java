package com.juanjob.app.database;

public class CustomerTable {

    public String getFirstname() {
        return firstname;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getAddress() {
        return address;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getFront_id() {
        return front_id;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public String getBack_id() {
        return back_id;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getBrgy() {
        return brgy;
    }

    public String getAccount_status() {
        return account_status;
    }

    public String getOnline_status() {
        return online_status;
    }

    String firstname;
    String middle_name;
    String lastname;
    String address;
    String mobile;
    String username;
    String password;
    String birthday;
    int age;
    String front_id;
    String back_id;
    String email;
    String gender;
    String profile_img;
    String brgy;
    String account_status;
    String online_status;

    public CustomerTable() {

    }

    public CustomerTable(String firstname, String middle_name, String lastname, String address, String mobile,
                       String username, String password, String birthday, int age, String front_id, String back_id,
                         String email, String gender, String profile_img, String brgy, String account_status, String online_status) {
        this.firstname = firstname;
        this.middle_name = middle_name;
        this.lastname = lastname;
        this.address = address;
        this.mobile = mobile;
        this.username = username;
        this.password = password;
        this.birthday = birthday;
        this.age = age;
        this.front_id = front_id;
        this.back_id = back_id;
        this.email = email;
        this.gender = gender;
        this.profile_img = profile_img;
        this.brgy = brgy;
        this.account_status = account_status;
        this.online_status = online_status;
    }
}
