package ru.doccloud.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by ilya on 3/6/18.
 */
public interface CommonService<T> {

    public T delete(final UUID id, String type) throws Exception;

    public List<T> findAll()  throws Exception;

    @Transactional(readOnly = true)
    List<T> findBySearchTerm(String searchTerm, Pageable pageable)  throws Exception;

    T add(final T dto, final String user) throws Exception;

    public T update(final T updated, final String user) throws Exception;


    public Page<T> findAll(Pageable pageable, String query)  throws Exception;


    public Page<T> findAllByType(final String type, final String[] fields, final Pageable pageable, final String query)  throws Exception;

    public Page<T> findAllByParentAndType(UUID parentid, String type, final Pageable pageable)  throws Exception;


    @Transactional
    void setUser();

    @Transactional
    void setUser(String userName);
}
