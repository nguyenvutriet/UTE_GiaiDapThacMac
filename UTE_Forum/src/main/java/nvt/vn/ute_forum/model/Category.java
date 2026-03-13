package nvt.vn.ute_forum.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
public class Category {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "isactive", nullable = false)
    private Boolean isActive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "categorycontainrequest", joinColumns = {@JoinColumn(name = "category_id", referencedColumnName = "id")},
    inverseJoinColumns = {@JoinColumn(name = "request_id", referencedColumnName = "id")})
    private List<Request> requests = new ArrayList<>();


    public Category() {
    }

    public Category(String id, String subject, Boolean isActive) {
        this.id = id;
        this.subject = subject;
        this.isActive = isActive;
    }

    public Category(String id, String subject, Boolean isActive, List<Request> requests) {
        this.id = id;
        this.subject = subject;
        this.isActive = isActive;
        this.requests = requests;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }


}
