package ru.doccloud.webapp.audit.aspect;

import com.fasterxml.jackson.databind.JsonNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.doccloud.service.AuditService;
import ru.doccloud.service.SystemCrudService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.webapp.audit.aspect.util.AuditEvents;
import ru.doccloud.webapp.audit.aspect.util.EventType;
import ru.doccloud.webapp.audit.aspect.util.EventTypeBean;
import ru.doccloud.webapp.audit.aspect.util.ParamBean;

import java.util.Date;
import java.util.Set;

import static ru.doccloud.webapp.audit.aspect.util.AuditHelper.*;

abstract class CommonDocAudit extends Audit {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonDocAudit.class);


    private final SystemCrudService systemCrudService;

    private final static String AUDIT_JSON_KEY = "audit";

    CommonDocAudit(SystemCrudService systemCrudService, AuditService auditService) {
        super(auditService);
        this.systemCrudService = systemCrudService;
    }


    @Override
    Object sentAuditResult2Elastic(ProceedingJoinPoint joinPoint, AuditEvents auditEvent, String user) throws Exception {
        LOGGER.debug("entering sentAuditResult2Elastic(auditEvent = {})", auditEvent);


        String exceptionText = null;

        Object[] params = null;

        String[] parameterNames = null;

        long elapsedTime = 0;

        Object o = null;

        boolean exceptionThrown = false;

        try {

            long start = System.currentTimeMillis();

            params = joinPoint.getArgs();

            LOGGER.trace("sentAuditResult2Elastic(): method params {}", params);

            if(user == null) {
                final ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                user = getUser(sra);
            }

            // name of method
            final MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            LOGGER.trace("sentAuditResult2Elastic(): method signature {}", signature);
            parameterNames = signature.getParameterNames();

            LOGGER.trace("sentAuditResult2Elastic(): parameterNames {}", parameterNames);

            o = joinPoint.proceed();

            LOGGER.trace("sentAuditResult2Elastic(): found dto {}", o);

            elapsedTime = System.currentTimeMillis() - start;


        } catch (Throwable e) {
            exceptionText = e.getMessage();
            exceptionThrown = true;
            throw new Exception(e);
        }
        finally {
            String jsonResponse= null;
            final Set<ParamBean> paramBeen = getParamValues(parameterNames, params);
            if(exceptionThrown) {
                LOGGER.trace("sentAuditResult2Elastic(): params {}, \n exceptionThrown {}",
                        paramBeen, exceptionThrown);
                jsonResponse = buildJsonResponse(auditEvent, paramBeen,
                        user ,exceptionText,  new Date());

                LOGGER.trace("sentAuditResult2Elastic():   params {}, \n jsonResponse {}",
                        paramBeen, jsonResponse);

                auditService.sendResponse(jsonResponse, "error");
            }

            else if (o != null) {

                DocumentDTO dto = (DocumentDTO) o;
                final JsonNode node = getAuditSettings(dto.getType());

                if(node != null) {
                    Set<EventTypeBean>  eventTypeBeanSet = getAuditPossibleEvents(node, auditEvent);

                    LOGGER.trace("sentAuditResult2Elastic():  audit events {}", eventTypeBeanSet);

                    final boolean isAuditEnabled = isAuditEnabled(eventTypeBeanSet);

                    LOGGER.trace("sentAuditResult2Elastic():  isAuditEnabled {}", isAuditEnabled);

                    if(isAuditEnabled) {

                        jsonResponse = buildJsonResponse(auditEvent, paramBeen, eventTypeBeanSet,
                                dto, user, exceptionText, elapsedTime, new Date());

                        LOGGER.trace("sentAuditResult2Elastic(): settings node for the document {} is \n{}, \n available events {}, \n params {}, \n jsonResponse {}",
                                dto, node, eventTypeBeanSet, paramBeen, jsonResponse);

                        auditService.sendResponse(jsonResponse, dto.getAuditIndexName());
                    }
                } else {
                    LOGGER.trace("sentAuditResult2Elastic(): no audit settings found for document {}", dto);
                }
            }
        }
        LOGGER.debug("leaving sentAuditResult2Elastic(): object {}", o);
        return o;
    }

    private boolean isAuditEnabled(Set<EventTypeBean> eventTypeBeanSet) {
        for (EventTypeBean eventTypeBean : eventTypeBeanSet) {
            if ((eventTypeBean.getEventType().equals(EventType.EVENT) ||
                    eventTypeBean.getEventType().equals(EventType.TIMING)) && eventTypeBean.isEnabled())
            return true;
        }
        return false;
    }

    private JsonNode getAuditSettings(final String docType) throws Exception {
        final SystemDTO systemDTO = (SystemDTO) systemCrudService.findBySymbolicName(docType);

        final JsonNode settingsNode = systemDTO.getData();

        return settingsNode.get(AUDIT_JSON_KEY);
    }
}
