package com.taipeigo.ticket.model;

import java.io.File;
import java.io.IOException;
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
		Optional<TicketVO> optional=ticketRepository.findById(ticketId);
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
        
        // 寫入建立時間
    	java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
    	ticketVO.setCreatedAt(now);
    	ticketVO.setUpdatedAt(now);
    	
        // 儲存商品基本欄位，讓資料庫生成該筆 ticketId
        TicketVO savedTicket = ticketRepository.save(ticketVO);

        if (files == null || files.length == 0) return;

        // 門票圖片資料夾路徑 (對應 C:/taipeiGo_uploads/images/ticket/)
        String subDir = "ticket/";
        File targetDir = new File(uploadBaseDir + subDir);

        // 若第一次執行，自動在 images 內部建立 ticket 資料夾
        if (!targetDir.exists()) {
            targetDir.mkdirs(); 
        }

        // 迴圈解開前端排好順序、過濾乾淨的圖片陣列
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                
                // 切下原始副檔名，利用 UUID 生成全新隨機檔名防重複
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
