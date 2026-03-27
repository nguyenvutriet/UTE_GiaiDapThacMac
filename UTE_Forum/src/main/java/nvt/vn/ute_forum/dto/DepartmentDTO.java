package nvt.vn.ute_forum.dto;

import nvt.vn.ute_forum.model.Department;

public class DepartmentDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String description;
    private Boolean isActive;

    // 1. Constructor: Chuyển dữ liệu từ Entity sang DTO
    public DepartmentDTO(Department dept) {
        if (dept != null) {
            this.id = dept.getId();
            this.name = dept.getName();
            this.email = dept.getEmail();
            this.phone = dept.getPhone();
            this.location = dept.getLocation();
            this.description = dept.getDescription();
            // Bà kiểm tra xem trong Entity Department của bà
            // tên hàm là getActive() hay getIsActive() nhé!
            this.isActive = dept.getActive();
        }
    }

    // 2. Getter và Setter (Bắt buộc phải có để Jackson đọc được dữ liệu)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}