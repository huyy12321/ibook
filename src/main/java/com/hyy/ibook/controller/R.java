package com.hyy.ibook.controller;


import java.io.Serializable;

public class R<T> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * 业务错误码
     */
    private long code;
    /**
     * 结果集
     */
    private T data;

    private long count;
    /**
     * 描述
     */
    private String msg;

    public R() {
        // to do nothing
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public static <T> com.hyy.ibook.controller.R<T> ok(T data) {
        com.hyy.ibook.controller.R<T> objectR = new com.hyy.ibook.controller.R<>();
        objectR.setCode(0);
        objectR.setData(data);
        return objectR;
    }

    public static <T> com.hyy.ibook.controller.R<T> ok(T data,long count) {
        com.hyy.ibook.controller.R<T> objectR = new com.hyy.ibook.controller.R<>();
        objectR.setCode(0);
        objectR.setCount(count);
        objectR.setData(data);
        return objectR;
    }


}

