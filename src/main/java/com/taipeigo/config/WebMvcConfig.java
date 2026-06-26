package com.taipeigo.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /*
     注入在 application.properties 設定的 taipeigo.upload.base-dir
     目前是 C:/taipeiGo_uploads/images/
    */
    @Value("${taipeigo.upload.base-dir}")
    private String uploadBaseDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 確保開頭有 file:// 
    	// 子資料夾路徑會變成：file://C:/taipeiGo_uploads/images/ticket/
    	String ticketLocation = Paths.get(uploadBaseDir, "ticket").toUri().toString();
        
        if (!ticketLocation.endsWith("/")) {
            ticketLocation += "/";
        }
        
        // 只有網址是 /images/ticket/**，才去 ticket 資料夾找圖
        registry.addResourceHandler("/images/ticket/**")
                .addResourceLocations(ticketLocation);
        
        /*
         System.out.println("==================================================");
         System.out.println("  ticketLocation: " + ticketLocation);
         System.out.println("==================================================");
        */
     // ====== 加入客服系統 (cs) 的圖片路徑對應 ======
        String csLocation = Paths.get(uploadBaseDir, "cs").toUri().toString();
        if (!csLocation.endsWith("/")) {
            csLocation += "/";
        }
        registry.addResourceHandler("/images/cs/**")
                .addResourceLocations(csLocation);
    }
}
