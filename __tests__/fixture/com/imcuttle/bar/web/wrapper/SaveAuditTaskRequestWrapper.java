package com.imcuttle.bar.web.wrapper;

import com.imcuttle.bar.data.SaveAuditTaskRequest;
import com.imcuttle.bar.web.data.SaveAuditTaskRequestVO;
import org.springframework.beans.BeanUtils;

public class SaveAuditTaskRequestWrapper {

    public static SaveAuditTaskRequest wrap(SaveAuditTaskRequestVO requestVO) {
        SaveAuditTaskRequest request = new SaveAuditTaskRequest();
        if (requestVO != null) {
            BeanUtils.copyProperties(requestVO, request);
        }
        return request;
    }
}
