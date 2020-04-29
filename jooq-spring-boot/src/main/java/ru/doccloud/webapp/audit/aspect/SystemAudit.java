package ru.doccloud.webapp.audit.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import ru.doccloud.document.controller.SystemController;
import ru.doccloud.service.AuditService;
import ru.doccloud.webapp.audit.aspect.util.AuditEvents;


@Component
@Aspect
@ConditionalOnExpression("'${aspect.audit.enabled}'=='true'")
//@ConditionalOnProperty(prefix="aspect.", name="enabled")
public class SystemAudit extends Audit {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemAudit.class);

    @Autowired
    public SystemAudit(AuditService auditService) {
        super( auditService);
    }
// find poitcuts
    @Pointcut("execution(* *..findById*(..)), * *..findBySymbolicName*(..)), * *..getUser(..)), * *..getGroup(..))")
    public void findSystemDocument() {
    }


//    @Pointcut("execution(* *..findBySymbolicName*(..))")
//    public void findBySymbolicName() {}


    @Pointcut("execution(* *..findBySearchTerm*(..)), * *..findParents(..)), * *..findAll*(..)), * *..getTypes*(..))")
    public void findCollectionsSystemDocument() {
    }

//    add pointcut
    @Pointcut("execution(* *..add*(..))")
    public void addMethods() {}


//    updateSystemDto pointcut
    @Pointcut("execution(* *..update*(..)), * *..setParent(..))")
    public void updateSystemMethods() {
    }

//    delete pointcut
    @Pointcut("execution(* *..delete(..))")
    public void deleteMethods() {}

// only public methods should be taken into account
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}


    @Around("publicMethod() && findCollectionsSystemDocument() && target(systemController) ")
    public Object findListAudit(ProceedingJoinPoint joinPoint, SystemController systemController) throws Throwable {
//
       return sentAuditResult2Elastic4Collections(joinPoint, null);
    }


    @Around("publicMethod() && findSystemDocument() && target(systemController)")
    public Object findSystemAudit(ProceedingJoinPoint joinPoint, SystemController systemController) throws Throwable {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.READ, null);
    }


    @Around("publicMethod() && addMethods() && target(systemController)")
    public Object createAudit(ProceedingJoinPoint joinPoint, SystemController systemController) throws Throwable {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.CREATE, null);
    }


    @Around("publicMethod() && deleteMethods() && target(systemController)")
    public Object deleteAudit(ProceedingJoinPoint joinPoint, SystemController systemController) throws Throwable {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.DELETE, null);
    }

    @Around("publicMethod() && updateSystemMethods() && target(systemController)")
    public Object updateAudit(ProceedingJoinPoint joinPoint, SystemController systemController) throws Throwable {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.UPDATE, null);
    }



}
