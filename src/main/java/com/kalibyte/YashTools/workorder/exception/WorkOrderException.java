package com.kalibyte.YashTools.workorder.exception;

import com.kalibyte.YashTools.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WorkOrderException extends BusinessException {
    public WorkOrderException(String message) {
        super(message);
    }
}
