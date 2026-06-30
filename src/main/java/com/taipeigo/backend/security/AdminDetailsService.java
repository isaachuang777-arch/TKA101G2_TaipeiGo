package com.taipeigo.backend.security;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.taipeigo.admin.model.AdminRepository;
import com.taipeigo.admin.model.AdminVO;

@Service
public class AdminDetailsService implements UserDetailsService {
    
    //要從DB撈資料
    @Autowired
    private AdminRepository adminRepository;

    @Override 
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //去DB用admAcc = username找人
        AdminVO adminVO = adminRepository.findByadmAcc(username);

        //防呆 (找不到人處理)
        if (adminVO == null){
            //錯誤 SS會自動redirect登入頁
        throw new UsernameNotFoundException("錯誤帳號或密碼，如有需要請聯絡IT管理員。");
    }

//Spring 的專屬權限
    List<GrantedAuthority> authorities = new java.util.ArrayList<>();

//3個可能是 1.要強制改密碼  2.正常登入 3.無權限的實習生
    if(adminVO.getAdmStatus() == adminVO.StatusForcetoChangePW) {
        authorities.add(new SimpleGrantedAuthority("ROLE_NEED_RESET_PW"));
    } else{ //正常登入 會去撈他的權限 再做ROLE
                //用forEach
                //SimpleGrantedAuthority 把字串壓成標準Role
                adminVO.getAdmPerVO().forEach(per ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + per.getAdmfuncVO().getFuncName()))
                );
                //無權限的
                authorities.add(new SimpleGrantedAuthority("ROLE_BASE_ADMIN"));
    
    }


    //判斷是不是啟用(Enable + 強改密碼都順利通過)
    boolean isEnabled = (adminVO.getAdmStatus() == adminVO.StatusEnabled || adminVO.getAdmStatus() == adminVO.StatusForcetoChangePW);

    return new User (
        adminVO.getAdmAcc(), //帳號
        adminVO.getAdmPw(), //密碼
        isEnabled,          //狀態 <-長期
        true, //帳號是否過期
        true, //密碼是否過期
        true, //狀態 <-短期
        authorities // Collection<GrantedAuthority>
    );
    }
}
