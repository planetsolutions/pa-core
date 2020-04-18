package ru.doccloud.webapp.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.doccloud.document.controller.AbstractController;

@Component
@Aspect
public class AuthorizationPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationPlugin.class);


    @Pointcut("execution(* ru.doccloud.document.controller.DocumentController.*(..)) || execution(* ru.doccloud.document.controller.IAController.*(..))")
    public void businessMethods() { }

    @Before("businessMethods() && target(documentController)")
    public void profile(JoinPoint joinPoint, AbstractController documentController) throws Throwable {
        long start = System.currentTimeMillis();
        logger.debug("AuthorizationPlugin: Going to call the method: {}", joinPoint.getSignature().getName());
        documentController.setUser();
//        Object output = pjp.proceed();
        logger.debug("AuthorizationPlugin: Method execution completed.");
        long elapsedTime = System.currentTimeMillis() - start;
        logger.debug("AuthorizationPlugin: Method execution time: " + elapsedTime + " milliseconds.");

//        return output;
    }
}

