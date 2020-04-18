package ru.doccloud.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import ru.doccloud.common.jooq.DocDsContext;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.repository.SystemRepository;

@Aspect
@Component
public class NoDefDsInterceptor implements Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoDefDsInterceptor.class);

    private final SystemRepository<SystemDocument> repository;

    private static final String DATASOURCE_NAME = "datasource";

    private int order;

    public NoDefDsInterceptor(SystemRepository<SystemDocument> repository) {
        this.repository = repository;
    }

    @Value("20")
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }

    @Around("@annotation(noDefDSConnection)  && args(type,..)\"")
    public Object proceed(ProceedingJoinPoint pjp, NoDefDSConnection noDefDSConnection, String type) throws Throwable {
        try {

            LOGGER.trace("proceed(): ds type : {}", type);
            String datasource = null;
            if (type!=null){
	            final SystemDocument systemDocument = repository.findBySymbolicName(type);
	
	            LOGGER.trace("proceed(): systemDocument {}", systemDocument);
	            final JsonNode data = systemDocument.getData().get(DATASOURCE_NAME);
	
	            LOGGER.trace("proceed(): json data {}", data);
	            datasource = data != null ? systemDocument.getData().get(DATASOURCE_NAME).asText() : null;
            }
            LOGGER.trace("proceed(): datasource {}", datasource);
            if(datasource != null)
                DocDsContext.setCurrentDS(datasource);
            Object result = pjp.proceed();
            return result;
        } finally {
            // restore state
            DocDsContext.clear();
        }
    }
}
