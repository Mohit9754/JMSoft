package com.jmsoft.Utility.pdf_helper;

/**
 * Created by Gk Emon on 8/30/2020.
 */
public class FailureResponse {
    Throwable throwable;
    String errorMessage;

    public FailureResponse(Throwable throwable) {
        this.throwable = throwable;
        if(throwable!=null)errorMessage=throwable.getLocalizedMessage();
    }
    public FailureResponse(Throwable throwable, String errorMessage) {
        this.throwable = throwable;
        this.errorMessage=errorMessage;
    }
    public FailureResponse(String errorMessage) {
        this.errorMessage=errorMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
