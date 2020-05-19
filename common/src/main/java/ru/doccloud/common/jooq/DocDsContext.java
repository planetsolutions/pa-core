package ru.doccloud.common.jooq;

public class DocDsContext {
    private static ThreadLocal<Object> currentDS = new ThreadLocal<>();

    public static void setCurrentDS(Object docType) {
        currentDS.set(docType);
    }

    public static Object getCurrentDS() {
        return currentDS.get();
    }

    public static void clear() {
        currentDS.remove();
    }
}
