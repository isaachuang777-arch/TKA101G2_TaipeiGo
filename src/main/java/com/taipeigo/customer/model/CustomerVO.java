package com.taipeigo.customer.model;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "customer")
public class CustomerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUST_ID", updatable = false)
    private Integer custId;

    // ===== 基本資料 =====

    @NotBlank(message = "姓名不可空白")
    @Size(min = 2, max = 20, message = "姓名長度需 2~20 字元")
    @Column(name = "CUST_NAME", nullable = false)
    private String custName;

    @NotBlank(message = "性別不可空白")
    @Pattern(regexp = "^[mfMF]$", message = "性別格式錯誤")
    @Column(name = "CUST_SEX", nullable = false)
    private String custSex;

    // ===== 聯絡資訊 =====

    /**
     * 台灣手機號碼：09 開頭，共 10 碼
     */
    @NotBlank(message = "手機號碼不可空白")
    @Pattern(regexp = "^09[0-9]{8}$", message = "請輸入正確台灣手機格式 (09xxxxxxxx)")
    @Column(name = "CUST_TEL", nullable = false)
    private String custTel;

    @NotBlank(message = "Email 不可空白")
    @Email(message = "Email 格式錯誤，必須包含 @ 符號")
    @Size(max = 100, message = "Email 長度不可超過 100 字元")
    @Column(name = "CUST_EMAIL", nullable = false, unique = true)
    private String custEmail;

    @Size(max = 200, message = "地址長度不可超過 200 字元")
    @Column(name = "CUST_ADDRESS")
    private String custAddress;

    // ===== 身分資訊 =====

    /**
     * 台灣身分證字號：
     *   - 第1碼：英文大寫 A~Z（排除 I、O）
     *   - 第2碼：1（男）或 2（女）
     *   - 第3~10碼：數字
     * 完整 checksum 驗證建議在 Service 層實作
     */
    @NotBlank(message = "身分證字號不可空白")
    @Pattern(
        regexp = "^[A-HJ-NPR-Z][12][0-9]{8}$",
        message = "身分證字號格式錯誤（範例：A123456789）"
    )
    @Column(name = "CUST_ID_CARD", nullable = false, unique = true)
    private String custIdCard;

    @NotNull(message = "生日不可空白")
    @Past(message = "生日必須是過去的日期")
    @Column(name = "CUST_BIRTHDAY", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate custBirthday;

    // ===== 帳號資訊 =====

    /**
     * 帳號規則：只允許英文字母與數字，長度 6~20
     */
    @NotBlank(message = "帳號不可空白")
    @Pattern(
        regexp = "^[A-Za-z0-9]{6,20}$",
        message = "帳號只能包含英文字母與數字，長度 6~20"
    )
    @Column(name = "CUST_ACCOUNT", nullable = false, unique = true)
    private String custAccount;

    /**
     * 密碼規則：
     *   - 長度 8~20
     *   - 必須包含至少一個英文大寫字母
     *   - 必須包含至少一個英文小寫字母
     *   - 必須包含至少一個數字
     *   - 不允許特殊字元
     */
    @NotBlank(message = "密碼不可空白")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$",
        message = "密碼需 8~20 字元，且必須包含大寫、小寫英文字母及數字，不可包含特殊字元"
    )
    @Column(name = "CUST_PASSWORD", nullable = false)
    private String custPassword;

    // ===== 其他 =====

    @Column(name = "CUST_IMG")
    private String custImg;

    @Column(name = "CUST_STATUS", nullable = false)
    private Integer custStatus;

    // ===== Getters & Setters =====

    public Integer getCustId() { return custId; }
    public void setCustId(Integer custId) { this.custId = custId; }

    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }

    public String getCustSex() { return custSex; }
    public void setCustSex(String custSex) { this.custSex = custSex; }

    public String getCustTel() { return custTel; }
    public void setCustTel(String custTel) { this.custTel = custTel; }

    public String getCustEmail() { return custEmail; }
    public void setCustEmail(String custEmail) { this.custEmail = custEmail; }

    public String getCustAddress() { return custAddress; }
    public void setCustAddress(String custAddress) { this.custAddress = custAddress; }

    public String getCustIdCard() { return custIdCard; }
    public void setCustIdCard(String custIdCard) { this.custIdCard = custIdCard; }

    public LocalDate getCustBirthday() { return custBirthday; }
    public void setCustBirthday(LocalDate custBirthday) { this.custBirthday = custBirthday; }

    public String getCustAccount() { return custAccount; }
    public void setCustAccount(String custAccount) { this.custAccount = custAccount; }

    public String getCustPassword() { return custPassword; }
    public void setCustPassword(String custPassword) { this.custPassword = custPassword; }

    public String getCustImg() { return custImg; }
    public void setCustImg(String custImg) { this.custImg = custImg; }

    public Integer getCustStatus() { return custStatus; }
    public void setCustStatus(Integer custStatus) { this.custStatus = custStatus; }
}
