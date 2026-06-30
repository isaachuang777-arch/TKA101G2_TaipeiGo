package com.taipeigo.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.taipeigo.admin.model.AdminRepository;
import com.taipeigo.admin.model.AdminVO;

//這是設定檔
@Configuration
//[好讀]Springboot看到pom檔 再找到這Config就會自動開 @EnableWebSecurity 但為了一開始就知道是WebSecuity 就加上去
@EnableWebSecurity
public class BackendSecurityConfig {

    @Autowired
    private AdminRepository adminRepository;


//這是Spring Secuity的密碼加密器
@Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

//攔截規則
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
    //=======================
    //權限設定
    //=======================
    http.authorizeHttpRequests(
    //免登入  
    authorize -> authorize.requestMatchers("/css/**","/js/**","/images/**","/backend/auth/login").permitAll()
    
    //改密碼頁
    .requestMatchers("/backend/admin/profile/resetPw").hasAnyRole("NEED_RESET_PW", "SuperAdmin", "訂單部", "客服中心", "資訊部","BASE_ADMIN")
    // 實習生也能進首頁跟查看個人資料
    .requestMatchers("/backend/admin/profile/**", "/backend/dashboard/**").hasRole("BASE_ADMIN")
    //讓所有登入的人都能看 403 畫面
    .requestMatchers("/backend/403").authenticated()

    //開始依照FuncName去限制權限
    //資訊部權限
    .requestMatchers("/backend/admin/it/**").hasAnyRole("資訊部", "SuperAdmin")
    
    //訂單部權限
    .requestMatchers("/backend/orders/**", "/backend/ticket/**", "/backend/ticketCategory/**", "/backend/activity/**", "/backend/product/**").hasAnyRole("訂單部", "SuperAdmin")

    //客服中心權限
    .requestMatchers("/backend/cs/**", "/backend/customer/**").hasAnyRole("客服中心", "SuperAdmin")


    .requestMatchers("/backend/**").hasAnyRole("SuperAdmin","訂單部", "客服中心", "資訊部","BASE_ADMIN")

    //上面沒有的~例如前台不管
    .anyRequest().permitAll()
    );
    //無權限的頁面會轉過去的頁面
    http.exceptionHandling(e -> e.accessDeniedPage("/backend/403"));

    //=======================
    //登入機制
    //=======================
    //沒有權限時要做什麼
    http.formLogin(form -> form
        //登入頁面
        .loginPage("/backend/auth/login")
        //虛擬頁 但要在login裡面 post那邊設定這虛擬頁
        .loginProcessingUrl("/backend/auth/loginProcess")
        //登入帳號
        .usernameParameter("admAcc")
        //登入密碼
        .passwordParameter("admPw")
        
        // //成功登入去的地方
        // .defaultSuccessUrl("/backend/dashboard/index",true)
        
        //由於後台功能都是吃loginSession才能吃登入VO 所以要改成 成功登入要做什麼
        .successHandler((request, response, authentication)-> {
            
            //從認證裡找到剛登入的admAcc
            String username = authentication.getName();
            //去DB找那個VO
            AdminVO adminVO = adminRepository.findByadmAcc(username);
            //把VO塞回session
            request.getSession().setAttribute("adminVO", adminVO);

            //強制改密碼 -> 
            if(adminVO.getAdmStatus() == adminVO.StatusForcetoChangePW){
                response.sendRedirect(request.getContextPath()+ "/backend/admin/profile/resetPw");
                //不能登入OWOb
                return;
            }

            //Spring 內建的登入跳轉回原路徑 -> 強登後跳轉功能 [原本的Spring Secuity是有這功能 但我改成 successHandler 所以要自己寫]
            //那是SpringSecuity的東西 叫SaveRequestAwareAuthenticationSuccessHandler 要用 所以要new一個
            org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler smartHarnHandler
            = new org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler();
            
            //告訴Handler 如果他是從登入頁進來就帶他去首頁
            smartHarnHandler.setDefaultTargetUrl("/backend/dashboard/index");

            //請handler做跳轉 ->看他有沒有存 登入前的頁面
            smartHarnHandler.onAuthenticationSuccess(request, response, authentication);

        })
        //TODO登入錯誤  => 也要在登入頁+error=true
        .failureUrl("/backend/auth/login?error=true")
        
    );
    //=======================
    //登出機制
    //=======================
    http.logout(logout -> logout
        //設定登出的路徑
        .logoutUrl("/backend/auth/logout")

        //登出後 把他導回登入畫面
        .logoutSuccessUrl("/backend/auth/login?logout=true")

        //保護前台session 不做整個session無效 [false]
        .invalidateHttpSession(false)
        //清除登入認證 = ture
        .clearAuthentication(true)

        //把Session裡的adminVO remove!
        .addLogoutHandler((request, response, authentication) ->{
            request.getSession().removeAttribute("adminVO");
        })
    );
    //由於要在form表單加入_csrf 前台又沒有設定 所以先把csrf關掉
    http.csrf(csrf -> csrf.disable());

    //打包 and 執行這包
    return http.build();

}
}
