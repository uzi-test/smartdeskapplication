package com.smartdesk.model.signup;

import com.google.firebase.firestore.DocumentReference;
import com.smartdesk.constants.Constants;
import com.google.firebase.firestore.ServerTimestamp;
import com.smartdesk.model.SmartDesk.UserBookDate;

import java.util.Date;
import java.util.List;

public class SignupUserDTO {

    String workerName;
    String workerPhone;
    String workerEmail;
    String workerDob;
    String workerGender;
    String workerPassword;
    Double workerLat;
    Double workerLng;
    String workerLocation;

    String profilePicture;

    Integer role = Constants.deskUserRole;
    String uuID;
    String userStatus;

    //Worker docs
    String workerDocumentID;

    //local variable
    String localDocuementID;
    Double distance;

    @ServerTimestamp
    Date registrationDate;

    public List<UserBookDate> bookDate;


    public SignupUserDTO(){
    }

    public SignupUserDTO(SignupUserDTO obj) {
        this.setWorkerName(obj.getWorkerName());
        this.setWorkerPhone(obj.getWorkerPhone());
        this.setWorkerDob(obj.getWorkerDob());
        this.setWorkerGender(obj.getWorkerGender());
        this.setWorkerPassword(obj.getWorkerPassword());
        this.setWorkerLat(obj.getWorkerLat());
        this.setWorkerLng(obj.getWorkerLng());

        this.setWorkerLocation(obj.getWorkerLocation());

        this.setProfilePicture(obj.getProfilePicture());

        this.setRole(obj.getRole());
        this.setUuID(obj.getUuID());
        this.setUserStatus(obj.getUserStatus());


        this.setLocalDocuementID(obj.getLocalDocuementID());

        this.setDistance(obj.getDistance());
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public void setWorkerPhone(String workerPhone) {
        this.workerPhone = workerPhone;
    }

    public String getWorkerEmail() {
        return workerEmail;
    }

    public void setWorkerEmail(String workerEmail) {
        this.workerEmail = workerEmail;
    }

    public String getWorkerDob() {
        return workerDob;
    }

    public void setWorkerDob(String workerDob) {
        this.workerDob = workerDob;
    }

    public String getWorkerGender() {
        return workerGender;
    }

    public void setWorkerGender(String workerGender) {
        this.workerGender = workerGender;
    }

    public String getWorkerPassword() {
        return workerPassword;
    }

    public void setWorkerPassword(String workerPassword) {
        this.workerPassword = workerPassword;
    }

    public Double getWorkerLat() {
        return workerLat;
    }

    public void setWorkerLat(Double workerLat) {
        this.workerLat = workerLat;
    }

    public Double getWorkerLng() {
        return workerLng;
    }

    public void setWorkerLng(Double workerLng) {
        this.workerLng = workerLng;
    }

    public String getWorkerLocation() {
        return workerLocation;
    }

    public void setWorkerLocation(String workerLocation) {
        this.workerLocation = workerLocation;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getUuID() {
        return uuID;
    }

    public void setUuID(String uuID) {
        this.uuID = uuID;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }


    public String getLocalDocuementID() {
        return localDocuementID;
    }

    public void setLocalDocuementID(String localDocuementID) {
        this.localDocuementID = localDocuementID;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getWorkerDocumentID() {
        return workerDocumentID;
    }

    public void setWorkerDocumentID(String workerDocumentID) {
        this.workerDocumentID = workerDocumentID;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public List<UserBookDate> getBookDate() {
        return bookDate;
    }

    public void setBookDate(List<UserBookDate> bookDate) {
        this.bookDate = bookDate;
    }
}