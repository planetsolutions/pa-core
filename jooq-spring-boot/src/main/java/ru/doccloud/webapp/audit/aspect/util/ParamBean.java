package ru.doccloud.webapp.audit.aspect.util;

import java.util.Objects;

/**
 * Created by Illia_Ushakov on 3/17/2018.
 */
public class ParamBean {
    private String paramName;
    private Object paramValue;

    ParamBean(String paramName, Object paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    String getParamName() {
        return paramName;
    }

    Object getParamValue() {
        return paramValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamBean)) return false;
        ParamBean paramBean = (ParamBean) o;
        return Objects.equals(paramName, paramBean.paramName) &&
                Objects.equals(paramValue, paramBean.paramValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName);
    }

    @Override
    public String toString() {
        return "ParamBean{" +
                "paramName='" + paramName + '\'' +
                ", paramValue=" + paramValue +
                '}';
    }
}
