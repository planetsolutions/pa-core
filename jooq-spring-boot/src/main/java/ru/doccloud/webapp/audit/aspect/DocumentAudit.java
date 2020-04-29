package ru.doccloud.webapp.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import ru.doccloud.service.AuditService;
import ru.doccloud.service.DocumentCrudService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.webapp.audit.aspect.util.AuditEvents;


@Component
@Aspect
@ConditionalOnExpression("'${aspect.audit.enabled}'=='true'")
//@ConditionalOnProperty(prefix="aspect.", name="enabled")
public class DocumentAudit extends CommonDocAudit {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentAudit.class);

    @Autowired
    public DocumentAudit(AuditService auditService, SystemCrudService systemCrudService) {
        super(systemCrudService, auditService);
    }
// find poitcuts
    @Pointcut("execution(* *..findById*(..))")
    public void findDocument() {
    }


    @Pointcut("execution(* *..findBySearchTerm*(..)), * *..findParents(..)), * *..findAll*(..))")
    public void findCollectionsDocument() {
    }

//    add pointcut
    @Pointcut("execution(* *..add(..))")
    public void addMethods() {}


//    updateSystemDto pointcut
    @Pointcut("execution(* *..update*(..)), * *..setParent(..))")
    public void updateMethods() {
    }

//    delete pointcut
    @Pointcut("execution(* *..delete(..))")
    public void deleteMethods() {}

// only public methods should be taken into account
    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

//
    @AfterReturning("publicMethod() && findCollectionsDocument() && target(documentService) ")
    public Object findListAudit(JoinPoint joinPoint, DocumentCrudService<DocumentDTO> documentService) throws Throwable {
//

       return sentAuditResult2Elastic4Collections(joinPoint, documentService.getRequestUser());
    }
//
//
    @Around("publicMethod() && findDocument() && target(documentService)")
    public Object findDocumentAudit(ProceedingJoinPoint joinPoint, DocumentCrudService<DocumentDTO> documentService) throws Throwable  {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.READ, documentService.getRequestUser());
    }
//


    @Around("publicMethod() && addMethods() && target(documentService)")
    public Object createAudit(ProceedingJoinPoint joinPoint, DocumentCrudService<DocumentDTO> documentService) throws Throwable {


        return sentAuditResult2Elastic(joinPoint, AuditEvents.CREATE, documentService.getRequestUser());
    }


    @Around("publicMethod() && deleteMethods() && target(documentService)")
    public Object deleteAudit(ProceedingJoinPoint joinPoint, DocumentCrudService<DocumentDTO> documentService) throws Throwable {

        return sentAuditResult2Elastic(joinPoint, AuditEvents.DELETE, documentService.getRequestUser());
    }

    @Around("publicMethod() && updateMethods() && target(documentService)")
    public Object updateAudit(ProceedingJoinPoint joinPoint, DocumentCrudService<DocumentDTO> documentService) throws Throwable {
        return sentAuditResult2Elastic(joinPoint, AuditEvents.UPDATE, documentService.getRequestUser());
    }

}
