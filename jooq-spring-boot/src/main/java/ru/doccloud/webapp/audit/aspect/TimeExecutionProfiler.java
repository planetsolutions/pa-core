package ru.doccloud.webapp.audit.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@Aspect
@ConditionalOnExpression("'${aspect.timeprofiler.enabled}'=='true'")
public class TimeExecutionProfiler {

	 private static final Logger logger = LoggerFactory.getLogger(TimeExecutionProfiler.class);

    
	@Pointcut("execution(* ru.doccloud.document.controller.*.*(..))")
	public void businessMethods() { }
	
	@Around("businessMethods()")
	public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		long start = System.currentTimeMillis();
	    logger.debug("ServicesProfiler.profile(): Going to call the method: {}", pjp.getSignature().getName());
	    Object output = pjp.proceed();
	    logger.debug("ServicesProfiler.profile(): Method execution completed.");
	    long elapsedTime = System.currentTimeMillis() - start;
	    logger.debug("ServicesProfiler.profile(): Method execution time: " + elapsedTime + " milliseconds.");
	
	    return output;
	}
    
    
}
