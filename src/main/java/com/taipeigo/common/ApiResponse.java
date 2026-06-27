package com.taipeigo.common;

public class ApiResponse<T> {
    private String status; // "success" 或 "error"
    private String message; // 提示訊息
    private T data; // 裝資料的地方 (物件或陣列)

    public ApiResponse() {
    }

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 成功回應：自訂訊息與資料
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    /**
     * 成功回應：自訂資料和預設成功訊息
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "操作成功", data);
    }

    /**
     * 失敗回應：自訂錯誤訊息，無資料
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null);
    }
}
