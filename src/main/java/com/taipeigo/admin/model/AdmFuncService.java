package com.taipeigo.admin.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.admin.model.AdmFuncVO;
import com.taipeigo.admin.model.AdmFuncRepository;

@Service
public class AdmFuncService {
	
	@Autowired
	private AdmFuncRepository admFuncRepository;

	public List<AdmFuncVO> getAlladmFuncs(){
		return admFuncRepository.findAll();
	}
}
