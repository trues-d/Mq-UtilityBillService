package com.example.consumer.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException{
    private int code;

    public BizException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BizException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public BizException(Throwable cause, int code) {
        super(cause);
        this.code = code;
    }
}
