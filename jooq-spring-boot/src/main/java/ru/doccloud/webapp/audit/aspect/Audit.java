package ru.doccloud.webapp.audit.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.doccloud.service.AuditService;
import ru.doccloud.service.document.dto.AbstractDTO;
import ru.doccloud.webapp.audit.aspect.util.AuditEvents;
import ru.doccloud.webapp.audit.aspect.util.EventTypeBean;
import ru.doccloud.webapp.audit.aspect.util.ParamBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;

import static ru.doccloud.webapp.audit.aspect.util.AuditHelper.buildJsonResponse;
import static ru.doccloud.webapp.audit.aspect.util.AuditHelper.getFullEventTypeBeanSet;
import static ru.doccloud.webapp.audit.aspect.util.AuditHelper.getParamValues;

abstract class Audit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Audit.class);

    final AuditService auditService;

    Audit(AuditService auditService) {
        this.auditService = auditService;
    }

    Object sentAuditResult2Elastic(ProceedingJoinPoint joinPoint, AuditEvents auditEvent, String user) throws Exception {
        LOGGER.debug("entering sentAuditResult2Elastic(auditEvent = {})", auditEvent);

        AbstractDTO dto = null;

        String exceptionText = null;

        Object[] params = null;

        String[] parameterNames = null;

//        String user = null;

        long elapsedTime = 0;

        Object o = null;

        boolean exceptionThrown = false;

        try {

            long start = System.currentTimeMillis();

            params = joinPoint.getArgs();

            if(user == null) {
                final ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                user = getUser(sra);
            }


            // name of method
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            parameterNames = signature.getParameterNames();


            o = joinPoint.proceed();

            dto = (AbstractDTO) o;

            LOGGER.trace("sentAuditResult2Elastic(): found dto \n{}, \nparameter names: \n{}, \nmethod signature  \n{}, \nuser \n{}, \n method params \n", o, parameterNames, signature, user, params);

            elapsedTime = System.currentTimeMillis() - start;


        } catch (Throwable e) {
            exceptionText = e.getMessage();
            exceptionThrown = true;
            throw new Exception(e);
        }
        finally {
            Set<EventTypeBean> eventTypeBeanSet = getFullEventTypeBeanSet();
            String jsonResponse= null;
            final Set<ParamBean> paramBeen = getParamValues(parameterNames, params);
            if(exceptionThrown) {
                jsonResponse = buildJsonResponse(auditEvent, paramBeen,
                        user ,exceptionText,  new Date());

                LOGGER.trace("sentAuditResult2Elastic():   params {}, \n jsonResponse {}",
                        paramBeen, jsonResponse);

                auditService.sendResponse(jsonResponse, "error");
            }

            else if (o != null) {
                jsonResponse = buildJsonResponse(auditEvent, paramBeen, eventTypeBeanSet,
                        o, user, exceptionText, elapsedTime, new Date());

                auditService.sendResponse(jsonResponse,  dto.getAuditIndexName());
            }
        }
        LOGGER.debug("leaving sentAuditResult2Elastic(): object {}", o);
        return o;
    }

    Object sentAuditResult2Elastic4Collections(JoinPoint joinPoint, String user) throws Exception {
        LOGGER.debug("entering sentAuditResult2Elastic4Collections()");
        String exceptionText = null;

        Object[] params = null;

        String[] parameterNames = null;

//        String user = null;

        Object o = null;

        try {

            params = joinPoint.getArgs();

            if(user == null) {
                final ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                user = getUser(sra);
            }

            // name of method
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            parameterNames = signature.getParameterNames();

        } catch (Throwable e) {
            exceptionText = e.getMessage();
            throw new Exception(e);
        }
        finally {

            final Set<ParamBean> paramBeen = getParamValues(parameterNames, params);

            final  String jsonResponse = buildJsonResponse(AuditEvents.READ, paramBeen,
                    user ,exceptionText,  new Date());

            LOGGER.trace("sentAuditResult2Elastic4Collections():   params {}, \n jsonResponse {}",
                    paramBeen, jsonResponse);

            auditService.sendResponse(jsonResponse, "collection_documents");
        }

        LOGGER.debug("leaving sentAuditResult2Elastic4Collections()");
        return o;
    }

    private void traceAttributes(ServletRequestAttributes sra) {
        LOGGER.trace("traceAttributes():  attributes of request");
        for(String attrName : sra.getAttributeNames(RequestAttributes.SCOPE_REQUEST)) {
            LOGGER.trace("traceAttributes():  attribute name {}, attribute value {}", attrName, sra.getAttribute(attrName, RequestAttributes.SCOPE_REQUEST));
        }
        LOGGER.trace("traceAttributes():  attributes of session");
        for(String attrName : sra.getAttributeNames(RequestAttributes.SCOPE_SESSION)) {
            LOGGER.trace("traceAttributes():  attribute name {}, attribute value {}", attrName, sra.getAttribute(attrName, RequestAttributes.SCOPE_SESSION));
        }

        LOGGER.trace("traceAttributes():  attributes of global session");
        for(String attrName : sra.getAttributeNames(RequestAttributes.SCOPE_GLOBAL_SESSION)) {
            LOGGER.trace("traceAttributes():  attribute name {}, attribute value {}", attrName, sra.getAttribute(attrName, RequestAttributes.SCOPE_GLOBAL_SESSION));
        }

    }


    String getUser(ServletRequestAttributes sra){
        LOGGER.trace("getUser():  ServletRequestAttributes {}", sra);

        String user = null;
        if (sra!=null){
            if(LOGGER.isTraceEnabled())
                traceAttributes(sra);

            HttpServletRequest request = sra.getRequest();
            if (request!=null){
                LOGGER.trace("sentAuditResult2Elastic(): request.getRemoteUser() - {} ", request.getRemoteUser());
                user = request.getRemoteUser();

                LOGGER.trace("sentAuditResult2Elastic(): remoteUser {}", user);
            } else {
                LOGGER.trace("sentAuditResult2Elastic(): no htttpservletrequest");
            }
        }
        return user;
    }
}
