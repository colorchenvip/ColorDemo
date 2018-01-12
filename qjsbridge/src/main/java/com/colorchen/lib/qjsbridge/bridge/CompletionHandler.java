package com.colorchen.lib.qjsbridge.bridge;

public interface  CompletionHandler {
    void complete(String retValue);
    void complete();
    void setProgressData(String value);
}
