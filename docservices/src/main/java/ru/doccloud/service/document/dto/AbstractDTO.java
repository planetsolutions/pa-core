package ru.doccloud.service.document.dto;

import java.beans.Transient;

/**
 * Created by ilya on 4/23/18.
 */
public abstract class AbstractDTO {

    @Transient
    public abstract String getDto4Audit();

    @Transient
    public abstract String getAuditIndexName();

}
