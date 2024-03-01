package com.juanjob.app.database;

public class ClientTable {

    String firstname;
    String middle_name;

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

    public String getCategory() {
        return category;
    }

    public String getOther_category() {
        return other_category;
    }

    public String getService_location() {
        return service_location;
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

    public String getOnline_status() {
        return online_status;
    }

    public String getProfile_img() {
        return profile_img;
    }

    public String getAccount_status() {
        return account_status;
    }

    public String getBrgy_clearance() {
        return brgy_clearance;
    }

    public String getPolice_clearance() {
        return police_clearance;
    }

    public String getRecent_job_proof() {
        return recent_job_proof;
    }

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
    String category;
    String subcategory;
    String other_category;

    String service_location;
    String account_status;
    String online_status;
    String brgy_clearance;
    String police_clearance;
    String recent_job_proof;
    String is_available;
    String rating;
    int completed_orders;

    public int getCompleted_orders() {
        return completed_orders;
    }

    public String getRating_quantity() {
        return rating_quantity;
    }

    String rating_quantity;

    public String getRating() {
        return rating;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getIs_available() {
        return is_available;
    }

    public ClientTable() {

    }

    public ClientTable(String firtname, String middle_name, String lastname, String address, String mobile,
                         String username, String password, String birthday, int age, String front_id,
                       String back_id, String email, String gender, String profile_img, String category,
                       String other_category, String service_location, String account_status, String online_status,
                       String brgy_clearance, String police_clearance, String recent_job_proof, String is_available,
                       String rating, String rating_quantity, String subcategory, int completed_orders) {
        this.firstname = firtname;
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
        this.category = category;
        this.other_category = other_category;
        this.service_location = service_location;
        this.account_status = account_status;
        this.online_status = online_status;
        this.brgy_clearance = brgy_clearance;
        this.police_clearance = police_clearance;
        this.recent_job_proof = recent_job_proof;
        this.is_available = is_available;
        this.rating = rating;
        this.rating_quantity = rating_quantity;
        this.subcategory = subcategory;
        this.completed_orders = completed_orders;
    }
}
