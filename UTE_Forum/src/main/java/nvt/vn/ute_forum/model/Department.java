package nvt.vn.ute_forum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "department")
public class Department {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "location")
    private String location;

    @Column(name = "isactive", nullable = false)
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "department")
    private List<Users> users = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "department")
    private List<Request> requests = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "todepartment")
    private List<ForwardingLog> receivedForwardingLogs = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "fromdepartment")
    private List<ForwardingLog> sendedForwardingLogs = new ArrayList<>();

    public Department() {
    }

    public Department(String id, String name, String description, String email, String phone, String location, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.isActive = isActive;
    }

    public Department(String id, String name, String description, String email, String phone, String location, Boolean isActive, List<Users> users, List<Request> requests) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.isActive = isActive;
        this.users = users;
        this.requests = requests;
    }

    public Department(String id, String name, String description, String email, String phone, String location, Boolean isActive, List<Users> users, List<Request> requests, List<ForwardingLog> receivedForwardingLogs, List<ForwardingLog> sendedForwardingLogs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.isActive = isActive;
        this.users = users;
        this.requests = requests;
        this.receivedForwardingLogs = receivedForwardingLogs;
        this.sendedForwardingLogs = sendedForwardingLogs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public List<ForwardingLog> getReceivedForwardingLogs() {
        return receivedForwardingLogs;
    }

    public void setReceivedForwardingLogs(List<ForwardingLog> receivedForwardingLogs) {
        this.receivedForwardingLogs = receivedForwardingLogs;
    }

    public List<ForwardingLog> getSendedForwardingLogs() {
        return sendedForwardingLogs;
    }

    public void setSendedForwardingLogs(List<ForwardingLog> sendedForwardingLogs) {
        this.sendedForwardingLogs = sendedForwardingLogs;
    }
}
