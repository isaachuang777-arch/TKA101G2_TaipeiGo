package com.taipeigo.ticket.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

@Service
public class TicketService {

	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private TicketSerialRepository ticketSerialRepository;

	@Autowired
	private TicketImageRepository ticketImageRepository;
	
	@Value("${taipeigo.upload.base-dir}")
	private String uploadBaseDir;
	
	public List<TicketVO> getAll() {
		return ticketRepository.findAll();
	}
	
	public TicketVO getOneTicket(Integer ticketId) {
		Optional<TicketVO> optional = ticketRepository.findById(ticketId);
		return optional.orElse(null);
	}
	
	/* 新增門票序號 */
	@Transactional
	public void generateSerials(Integer ticketId, int quantity) {
		TicketVO ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new IllegalArgumentException("找不到該門票商品，編號: " + ticketId));

		for (int i = 0; i < quantity; i++) {
			TicketSerialVO serial = createNewAvailableSerial(ticket);
			ticketSerialRepository.save(serial);
		}
	}

	/**
	 * 新增門票序號物件
	 */
	private TicketSerialVO createNewAvailableSerial(TicketVO ticket) {
		TicketSerialVO serial = new TicketSerialVO();
		serial.setTicketVO(ticket);
		String uniqueCode = generateUniqueSerialNumber();
		serial.setSerialNumber(uniqueCode);
		serial.setCustomerVO(null);  
		serial.setStatus(0); 
		// 購買此門票序號時才會將使用日期代入，故用null
		serial.setExpiryDate(null);
		return serial;
	}

	/* 產生不重複門票序號，TK+9碼(如TKABC123XYZ) */
	public String generateUniqueSerialNumber() {
		String prefix = "TK";
		String serialNumber = "";
		boolean isDuplicate = true;

		while (isDuplicate) {
			// 用 UUID 先排除 "-" 並設定大寫後再取9碼
			String uuidStr = UUID.randomUUID().toString();
			String cleanCode = uuidStr.replace("-", "").toUpperCase();
			String randomCode = cleanCode.substring(0, 9);
			
			serialNumber = prefix + randomCode; 
			// 去資料庫驗證有沒有人用了
			isDuplicate = ticketSerialRepository.existsBySerialNumber(serialNumber);
		}
		return serialNumber;
	}
	
	/**
	 * 新增門票商品 
	 */
	@Transactional
	public void addTicketWithImages(TicketVO ticketVO, MultipartFile[] files) {
		
		// 寫入建立與修改時間
		java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
		ticketVO.setCreatedAt(now);
		ticketVO.setUpdatedAt(now);
		
		// 儲存商品基本欄位，讓資料庫生成該筆 ticketId
		TicketVO savedTicket = ticketRepository.save(ticketVO);

		// 上傳圖片
		if (files != null && files.length > 0) {
			saveImage(files, savedTicket);
		}
	}

	/**
	 * 修改/更新門票商品
	*/
    @Transactional
    public void updateTicketWithImages(TicketVO ticketVO, MultipartFile[] files, Integer[] deleteImageIds) {
        
        // 先執行前端要刪除的舊圖片(刪除資料夾檔案 + 在資料庫刪除該筆資料 )
        this.deleteImage(deleteImageIds);

        // 取出未刪的舊圖
        TicketVO existingTicket = ticketRepository.findById(ticketVO.getTicketId()).orElse(null);
        if (existingTicket != null) {
            // 從 DB 取出原有的所有圖片
            List<TicketImageVO> currentDbImages = new ArrayList<>(existingTicket.getTicketImages());
            
            // 原有圖片過濾那些已經被執行刪除的舊圖，留下還在的圖片
            if (deleteImageIds != null && deleteImageIds.length > 0) {
                List<Integer> deleteIdsList = Arrays.asList(deleteImageIds);
                currentDbImages.removeIf(img -> deleteIdsList.contains(img.getTicketImageId()));
            }
            
            // 舊圖清單重新存入的 ticketVO 中
            ticketVO.setTicketImages(currentDbImages);
        }

        // 更新最新修改時間
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        ticketVO.setUpdatedAt(now);
        
        // 儲存門票商品主體（確定要留下來的舊圖會存在 DB）
        TicketVO savedTicket = ticketRepository.save(ticketVO);

        // 若管理員有上傳新圖片，則進行新圖的儲存
        if (files != null && files.length > 0) {
            saveImage(files, savedTicket);
        }
    }

	/**
	 *  刪除圖片 （硬刪除）
	 */
	private void deleteImage(Integer[] deleteImageIds) {
		
		if (deleteImageIds == null || deleteImageIds.length == 0) {
			return; 
		}

		for (Integer imgId : deleteImageIds) {
			
			// 根據 ID 從 TICKET_IMAGE 表中撈出該張圖的資料
			ticketImageRepository.findById(imgId).ifPresent(img -> {
				String src = img.getTicketImageSrc();
				
				// 檢查：只有本機上傳的相對路徑才需要去資料夾刪除檔案
				if (src != null && src.startsWith("/images/ticket/")) {
					
					// 用最後一個 / 切出純檔名，例如 "abc123.jpg"
					String filename = src.substring(src.lastIndexOf("/") + 1);
					
					// 組合出資料夾內的完整路徑
					File diskFile = new File(uploadBaseDir + "ticket/" + filename);
					
					if (diskFile.exists()) {
						if (diskFile.delete()) {
							// System.out.println("刪除成功: " + filename);
						} else {
							// System.out.println("刪除失敗:" + filename);
						}
					} 
				}
			});

			ticketImageRepository.deleteById(imgId);
		}
	}

	/**
	 * 圖片存檔（在指定資料夾寫入圖片，並在DB寫入該圖片路徑)
	 */
	private void saveImage(MultipartFile[] files, TicketVO savedTicket) {
		String subDir = "ticket/";
		File targetDir = new File(uploadBaseDir + subDir);

		if (!targetDir.exists()) {
			targetDir.mkdirs(); 
		}

		for (MultipartFile file : files) {
			if (file != null && !file.isEmpty()) {
				
				String originalFilename = file.getOriginalFilename();
				String ext = "";
				if (originalFilename != null && originalFilename.contains(".")) {
					ext = originalFilename.substring(originalFilename.lastIndexOf("."));
				} else {
					ext = ".jpg"; // 防呆預設副檔名
				}
				String savedName = UUID.randomUUID().toString() + ext;

				try {
					// TODO: 將圖片實體寫入本機實體ticket 資料夾 (之後改由伺服器或雲端)
					File saveFile = new File(targetDir, savedName);
					file.transferTo(saveFile);

					// 產生相對網址，作為網頁前端讀取路徑
                    // 產出格式："/images/ticket/xxxxx.jpg" => 讓 WebMvcConfig 攔截導航
					String dbPath = "/images/" + subDir + savedName; 

					// 建立你的 TicketImageVO 物件並儲存
					TicketImageVO imgVO = new TicketImageVO();
					imgVO.setTicketImageSrc(dbPath);       // 寫入資料庫欄位的純相對路徑字串
                    imgVO.setTicketVO(savedTicket);        // 綁定剛剛save的商品物件做FK        
					
					ticketImageRepository.save(imgVO); 

				} catch (IOException e) {
					throw new RuntimeException("實體圖片儲存失敗: " + originalFilename, e);
				}
			}
		}
	}
}