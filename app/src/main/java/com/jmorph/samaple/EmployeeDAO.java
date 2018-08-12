package com.jmorph.samaple;

import com.jmorph.annotation.FieldTransformer;
import com.jmorph.annotation.MorphTo;
import com.jmorph.annotation.MorphToField;

import java.math.BigInteger;

@MorphTo(EmployeeDTO.class)
public class EmployeeDAO {
    private String firstName;
    private String lastName;
    private BigInteger idNumber;
    private String phone;
    private String email;
    private String jobTitle;
    @MorphToField("departmentName")
    @FieldTransformer(DepartmentIdToNameTransformer.class)
    private int departmentId;
    private String supervisor;
    @MorphToField("workLocation")
    private String location;
    private int employeeType;
    private boolean isActive;
    @FieldTransformer(MillisToDateStringTransformer.class)
    private long startDate;
    @FieldTransformer(MillisToDateStringTransformer.class)
    private long endDate;
    private String documentLink;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public BigInteger getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(BigInteger idNumber) {
        this.idNumber = idNumber;
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(int employeeType) {
        this.employeeType = employeeType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    @Override
    public String toString() {
        return "EmployeeDAO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", idNumber=" + idNumber +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", departmentId=" + departmentId +
                ", supervisor='" + supervisor + '\'' +
                ", location='" + location + '\'' +
                ", employeeType=" + employeeType +
                ", isActive=" + isActive +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", documentLink='" + documentLink + '\'' +
                '}';
    }
}
