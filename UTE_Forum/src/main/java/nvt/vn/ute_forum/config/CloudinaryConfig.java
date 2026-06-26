package nvt.vn.ute_forum.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Cloudinary — Spring @Bean mặc định là Singleton.
 * Chỉ khởi tạo 1 lần duy nhất, tái sử dụng toàn bộ ứng dụng.
 *
 * Cần thêm vào application.properties:
 *   cloudinary.cloud-name=your_cloud_name
 *   cloudinary.api-key=your_api_key
 *   cloudinary.api-secret=your_api_secret
 */
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }
}