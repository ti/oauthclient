package cm.lnx.auth.model;

import cm.lnx.auth.util.GsonUtils;


public class GenericResponse<T> {
    private int status;
    private String body;
    private T data;
    private Class<T> classOfT;
    /**
     * 生成标准的返回数据
     * @param status http返回的状态码，例如：200,400,401等。
     * @param body   http返回的数据字符串
     * @param classOfT 需要JSON格式化数据的class，如果不需要，可为空
     */
    public GenericResponse(int status, String body, Class<T> classOfT) {
        this.status = status;
        this.body = body;
        this.classOfT = classOfT;
    }

    /**
     * 请求是否成功
     * @return
     */
    public boolean isSucceed() {
        return (this.status >=200 && this.status <300);
    }

    /**
     * 返回字符串数据
     * @return
     */
    public String getBody() {
        return body;
    }

    /**
     * 返回的状态码
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * 返回的JSON数据
     * @return
     */
    public  T getData() {
        if (data == null && isSucceed()) {
            data = GsonUtils.fromJson(body,classOfT);
        }
        return data;
    }


}
