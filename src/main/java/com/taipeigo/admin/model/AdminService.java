package com.taipeigo.admin.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.admin.model.AdmFuncVO;
import com.taipeigo.admin.model.AdmPerVO;
import com.taipeigo.admin.model.AdminVO;
import com.taipeigo.admin.model.AdmPerRepository;
import com.taipeigo.admin.model.AdminRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class AdminService {

	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private AdmPerRepository admPerRepository;

	// getAllAdmin全查
	public List<AdminVO> getAllAdmin() {
		return adminRepository.findAll();
	}

// findByAdmId找流水號
	public AdminVO findByAdmId(Integer admId) {
		return adminRepository.findById(admId).orElse(null);
		// 找不到就是null
	}

// findByadmStatus找狀態
	public List<AdminVO> findByadmStatus(Byte admStatus) {
		return adminRepository.findByadmStatus(admStatus);
	}

// findByadmAcc找帳號
	public AdminVO findByadmAcc(String admAcc) {
		return adminRepository.findByadmAcc(admAcc);
	}

// Login登入時會先用VO裡面的getAdmStatus()看是不是停權了
	public AdminVO adminLogin(String admAcc, String admPw) {
		AdminVO vo = adminRepository.findByAdmAccAndAdmPw(admAcc, admPw);

//PW或空帳檢查
		if(vo == null) {
			throw new RuntimeException("錯誤帳號或密碼，如有需要請聯絡IT管理員。");
		}
		
		
//停權檢查
		if (vo.getAdmStatus() == 0) {
			throw new RuntimeException("此帳號已被停權！如有需要請聯絡IT管理員。");
		}
		return vo;
	}

// 建立新帳號+防呆+權限新增
	@Transactional // 因為牽涉到兩張表 (ADMIN 與 ADM_PER) 的新增
	public void createAdmin(AdminVO adminVO, Integer[] funcIds) {

//trim
        adminVO.setAdmAcc(adminVO.getAdmAcc().trim());
        adminVO.setAdmPw(adminVO.getAdmPw().trim());
        
// 限制admAcc
        if (!adminVO.getAdmAcc().matches("^[a-zA-Z0-9]+$")) {
            throw new RuntimeException("新增失敗：帳號只能包含英文與數字！");
        }
        
// 限制密碼格式(英文或數字，且至少8碼)
        if (!adminVO.getAdmPw().matches("^[\\s\\S]{8,}$")) {
            throw new RuntimeException("新增失敗：密碼必須為 8 碼以上的英文、數字或符號！");
        }

		
// 防呆 帳號是否已存在
		if (adminRepository.findByadmAcc(adminVO.getAdmAcc()) != null) {
			throw new RuntimeException("新增失敗：此帳號已存在！請重新輸入。");
		}
		//在Service先預設帳號建立時已開通
		adminVO.setAdmStatus((byte) 1); 
		// save
		adminRepository.save(adminVO);

// 權限新增
		if (funcIds != null && funcIds.length > 0) {
			for (Integer funcId : funcIds) {
				AdmPerVO perVO = new AdmPerVO();

				perVO.setAdminVO(adminVO);

				AdmFuncVO funcVO = new AdmFuncVO();
				funcVO.setFuncId(funcId);
				perVO.setAdmfuncVO(funcVO);

				admPerRepository.save(perVO);
			}
		}
	}
	
//Containing
	public List<AdminVO> findAdminsbyContaining(String keyword){
		//先做查詢
		  List<AdminVO> resultList = adminRepository.findByAdmAccContainingOrAdmNameContaining(keyword, keyword);
	        
	        //0resultmsg
	        if(resultList.isEmpty()) {
	            throw new RuntimeException("找不到相關人員，請重新輸入。");
	        }
	        
	        return resultList;
	}
//update2
@Transactional
	public void updateAdmin(AdminVO adminVO){

		//naming as dbAdminVO = I dont want to share PW to forntend page
		//adminVO is the original vo => the dbAdminVO got the full detail from adminVO
		AdminVO dbAdmin = adminRepository.findById(adminVO.getAdmId()).orElseThrow(() -> new RuntimeException("修改失敗"));

		//but I need to save a adminVO to DB with full detail. So I need to restrict setName date and Status. 
		//adminVO.getAdmName => frontend data
		dbAdmin.setAdmName(adminVO.getAdmName());
		dbAdmin.setHireDate(adminVO.getHireDate());
		dbAdmin.setAdmStatus(adminVO.getAdmStatus());

		//save(the dbAdminVO wif changed name/date/status) 
		adminRepository.save(dbAdmin);
		

	} 


//updateAdmStatus
@Transactional
	public void updateadmStatus(Integer admId, Byte newadmStatus){
		//dbAdmin got a full VO 
		AdminVO dbAdmin = adminRepository.findById(admId).orElseThrow(() -> new RuntimeException("狀態切換失敗"));
		//dbAdmin set the new Status 
		dbAdmin.setAdmStatus(newadmStatus);
		//saved a VO with newStatus and original data to db
		adminRepository.save(dbAdmin);

	}

//IT可以強制更改密碼 密碼會是admAcc+今日日期YYYYMMDD
@Transactional
	public void itforceResetPw(Integer admId){
		//防呆
		AdminVO adminVO = adminRepository.findById(admId).orElseThrow(() -> new RuntimeException("修改失敗：找不到帳號資料！"));

		//取得今天日期
		String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		
		//組新密碼
		String newAdmPw = adminVO.getAdmAcc() + todayStr;

		//OverWrite 密碼+改狀態
		adminVO.setAdmPw(newAdmPw);
		adminVO.setAdmStatus(AdminVO.StatusForcetoChangePW);

		//存進DB
		adminRepository.save(adminVO);

	}

//管理員改密碼
@Transactional
	public void adminResetPw(Integer admId, String oldPw, String newPw){
		AdminVO adminVO = adminRepository.findById(admId).orElseThrow(() -> new RuntimeException("修改失敗：找不到您的帳號資料！"));
		
		//防呆 再打一次密碼
		if (!adminVO.getAdmPw().equals(oldPw)) {
			throw new RuntimeException("修改失敗：您輸入的舊密碼不正確！");
		}

		
		//寫新密碼進去 status也改回enable 他就能進去了
		adminVO.setAdmPw(newPw);
		adminVO.setAdmStatus(AdminVO.StatusEnabled);

		adminRepository.save(adminVO);

	}
//============================
// 權限修改先刪除 後新增
//============================
@Transactional
	public void updateAdminPer(Integer admId, Integer[] funcIds){
		//刪除
		admPerRepository.deleteByAdminVO_AdmId(admId);
		//先把delete跑進db
	    admPerRepository.flush();
		//如果回傳的funcIds[]這陣列不是空的 就以做新增
		if(funcIds !=null && funcIds.length > 0){
			AdminVO adminVO= new AdminVO();
			adminVO.setAdmId(admId);
			//把funcIds[]抽出funcId做for loop
			for(Integer funcId : funcIds){
				AdmPerVO admPerVO = new AdmPerVO();
				//把adminVO[admId]餵給admPerVO
				admPerVO.setAdminVO(adminVO);

				AdmFuncVO funcVO =new AdmFuncVO();
				funcVO.setFuncId(funcId);
				admPerVO.setAdmfuncVO(funcVO);
				//存好餵給admPerRepo
				admPerRepository.save(admPerVO);

			}
		}
	}
//============================
// 列出權限by不同權限 使用 List<AdminVO> findByAdminByFuncId(Integer funcId);
//============================
	public List<AdminVO> getAdminByFuncId(Integer funcId){
		return adminRepository.findByAdminByFuncId(funcId);
	}
//================================
//分頁Service如下
//================================
//用admStatus找人 [分頁]
	public Page<AdminVO> getByadmStatusByPage (Byte admStatus, int pageNumber){
		Pageable pageable = PageRequest.of(pageNumber - 1, 10);
		return adminRepository.findByadmStatus(admStatus, pageable);

	}
//模糊searchName/acc [分頁]
	public Page<AdminVO> getByAdmAccContainingOrAdmNameContainingBypage(String keyword, int pageNumber){
	Pageable pageable = PageRequest.of(pageNumber - 1, 10);
	Page<AdminVO> pageResult = adminRepository.findByAdmAccContainingOrAdmNameContaining(keyword, keyword, pageable);
	        
	        //0resultmsg
	        if(pageResult.isEmpty()) {
	            throw new RuntimeException("找不到相關人員，請重新輸入。");
	        }
	        
	        return pageResult;
	}
 //找出XXX權限的管理員 [分頁]
//============================
	public Page<AdminVO> getByAdminByFuncIdByPage(Integer funcId, Integer pageNumber){
		Pageable pageable = PageRequest.of(pageNumber - 1, 10);
		return adminRepository.findDistinctByAdmPerVOs_AdmfuncVO_FuncId(funcId, pageable);
	}
//找沒有權限的管理員 [分頁]
	public Page<AdminVO> getByAdmPerVOisEmptyByPage(int pageNumber){
		Pageable pageable = PageRequest.of(pageNumber -1, 10);
		return adminRepository.findByAdmPerVOsIsEmpty(pageable);
	}
//全找管理員 [分頁]
	public Page<AdminVO> getAllAdminBypage(int pageNumber){
		Pageable pageable = PageRequest.of(pageNumber -1,10);
		return adminRepository.findAll(pageable);
	}
}
