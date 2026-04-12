# 🎓 Developing a student Q&A platform to support inquiries and reduce student dropout rates

## 📖 Introduction
In the digital age, the demand for information exchange and answering students’ questions is increasingly growing. However, many students face difficulties in finding support or feel hesitant to directly contact administrative departments.

👉 This project is developed to:
- Support students in asking questions quickly
- Enable direct connection with administrative departments
- Create a transparent and convenient communication environment
- Meet the digital transformation trends in education

---

## 🚀 Key Features
### 👨‍🎓 For Students
- Post questions
- View announcements from the institution
- Participate in forums
- Send private messages to departments
### 🏫 For Departments / Faculties / Institutes
- Manage announcements
- Respond to questions on the forum
- Handle private messages
- View statistics
### ⚙️ For Admins
- Manage forums
- Manage categories
- Handle violation reports
- Manage inquiries

---

## 🛠️ Technologies Used

| Component        | Technology |
|------------------|----------|
| Language         | ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white) |
| Framework        | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) |
| JDK              | ![JDK](https://img.shields.io/badge/JDK-21-007396?style=for-the-badge&logo=openjdk&logoColor=white) |
| Database         | ![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white) |
| Build Tool       | ![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white) |

---

## ⚙️ Installation & Running the Project

### 1. Clone project
```bash
   git clone https://github.com/nguyenvutriet/UTE_GiaiDapThacMac.git
```
### 2. Build  project
```bash
    mvn clean install
 ```
### 3. Config  ``` application.propertise ```
 ```bash 
    # Spring Datasource
    spring.datasource.url=jdbc:mysql://localhost:3306/<name_db>
    spring.datasource.username=<your_ussername>
    spring.datasource.password=<your_password>

    spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

    # Spring Security OAuth2 Client Configuration for Google
    spring.security.oauth2.client.registration.google.client-id=<your_client_id>
    spring.security.oauth2.client.registration.google.client-secret=<your_secret_id>

    # Spring Mail Configuration
    spring.mail.host=smtp.gmail.com
    spring.mail.port=587
    spring.mail.username=<your_email>
    spring.mail.password=<your_password_app>
    spring.mail.protocol=smtp
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true

    spring.servlet.multipart.max-file-size=20MB
    spring.servlet.multipart.max-request-size=20MB

    # Folder upload
    app.upload.dir=../uploads/
 ```

## 📚 Documentation & Demo
- 🌐 Demo: [online-hcmute-edu-vn](https://online-hcmute-edu-vn.duckdns.org/login)
- 🎥 Video demo: [Video](https://youtu.be/aWmb6k8u9hM)
- 📄 Detailed Documentation: [document](https://onedrive.live.com/:w:/g/personal/24e5b2f2c6c2d532/IQDRXsXDDHMSQr3G-VRotjA6ARFgyZWLfo-GEf35EY7aG2c?rtime=5vsIet-T3kg&redeem=aHR0cHM6Ly8xZHJ2Lm1zL3cvYy8yNGU1YjJmMmM2YzJkNTMyL0lRRFJYc1hEREhNU1FyM0ctVlJvdGpBNkFSRmd5WldMZm8tR0VmMzVFWTdhRzJjP2U9QzhTSk5P)

📌 Note: UML is used for system design.

## 📜 License
This project is the intellectual property of the author team. The source code is currently made public under the following conditions:

- 👀 **View Permission:** Users are allowed to view and reference the source code for learning purposes.  
- ✏️❌ **No Modification:** Copying, modifying, or creating derivative works is not permitted without prior consent from the author team.  
- 🚫📦 **No Distribution:** Sharing or redistributing the source code in any form for commercial purposes is strictly prohibited.  

📩 For any other usage requests, please contact the author team directly.

## 👨‍💻 Author Team
<table>
  <tr>
    <td align="center">
      <img src="./uploads/pthb.png" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/banghoang-hub">Phan Tong Hoang Bang</a></b><br><br>
    </td>
    <td align="center">
      <img src="./uploads/hgdn.png" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/NgocHuynh1509">Huynh Gia Diem Ngoc</a></b><br><br>
    </td>
        <td align="center">
      <img src="./uploads/vtmq.png" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/maiquynhvnlhb-ship-it">Vo Thi Mai Quynh</a></b><br><br>
    </td>
        <td align="center">
      <img src="./uploads/pttt.png" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/thtra-TT">Phan Thi Thanh Tra</a></b><br><br>
    </td>
        <td align="center">
      <img src="./uploads/nvt.jpg" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/nguyenvutriet">Nguyen Vu Triet</a></b><br><br>
    </td>
        <td align="center">
      <img src="./uploads/htt.png" width="150" style="border-radius: 8px;"><br><br>
      <b><a href="https://github.com/thanhtu05">Hoang Thanh Tu</a></b><br><br>
    </td>
  </tr>
</table>

 


