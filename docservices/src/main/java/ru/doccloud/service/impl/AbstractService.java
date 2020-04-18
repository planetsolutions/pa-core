package ru.doccloud.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.doccloud.document.model.AbstractDocument;
import ru.doccloud.repository.CommonRepository;

import javax.servlet.http.HttpServletRequest;

public class AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);

    private String currentUser;

    protected CommonRepository<? extends AbstractDocument> repository;

    AbstractService(CommonRepository<? extends AbstractDocument> repository) {
        this.repository = repository;
    }

//    @Transactional
//    public void setUser() {
//        LOGGER.debug("entering setUser()");
//
//        LOGGER.info("leaving setUser(): user {} ", user);
//        repository.setUser(user);
//
//    }

//    @Transactional
//    public void setUser(String userName) {
//        LOGGER.debug("setUser(userName={})", userName);
//        repository.setUser(userName);
//    }

    public void setUser() {
        LOGGER.debug("entering setUser()");
        repository.setUser(getRequestUser());

    }

    synchronized String getRequestUser(){
        if (this.currentUser==null){
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (sra!=null){
                HttpServletRequest request = sra.getRequest();
                if (request!=null){
                    LOGGER.info("getRequestUser(): request.getRemoteUser() - {} ", request.getRemoteUser());
                    this.currentUser = request.getRemoteUser();
                }
            }
        }
        return this.currentUser;

    }

    public void setUser(String userName) {
        LOGGER.debug("setUser(userName={})", userName);
        synchronized (this) {
            this.currentUser = userName;
            repository.setUser(userName);
        }
    }


}
