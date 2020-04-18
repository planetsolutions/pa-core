package ru.doccloud.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.JsonNode;

import ru.doccloud.common.exception.DocumentNotFoundException;

/**
 * Created by ilya on 6/3/17.
 */
public interface CommonRepository<T> {
//    public void setUser();

    public void setUser(String userName);

    /**
     * Adds a new SystemDocument.
     * @param systemEntry  The information of the added SystemDocument entry.
     * @return  The added SystemDocument entry.
     */
    public T add(String type, T systemEntry);

    /**
     * Deletes a SystemDocument entry.
     * @param type 
     * @param id    The id of the deleted SystemDocument entry.
     * @return  The deleted SystemDocument entry.
     * @throws DocumentNotFoundException If the deleted  entry is not found.
     */
    public T delete(String type, UUID id);

    /**
     * Finds all SystemDocument entries.
     * @return  Found SystemDocument entries.
     */
    public List<T> findAll();

    /**
     * Finds a SystemDocument entry.
     * @param id    The id of the requested SystemDocument entry.
     * @param type 
     * @return  The found SystemDocument entry.
     * @throws DocumentNotFoundException If SystemDocument entry is not found.
     */
    public T findById(String type, UUID id);

    /**
     * Finds a SystemDocument entry.
     * @param uuid    The uuid of the requested SystemDocument entry.
     * @return  The found SystemDocument entry.
     * @throws DocumentNotFoundException If SystemDocument entry is not found.
     */
    public T findByUUID(String uuid);

    public Page<T> findBySearchTerm(String searchTerm, Pageable pageable);


    /**
     * Updates the information of a Document entry.
     * @param todoEntry   The new information of a Document entry.
     * @return  The updated Document entry.
     * @throws DocumentNotFoundException If the updated Document entry is not found.
     */
    public T update(String type, T documentEntry);

    public T updateFileInfo(String type, T documentEntry);

    public Page<T> findAll(Pageable pageable, String query);


    public Page<T> findAllByType(String type, String[] fields, Pageable pageable, String query, JsonNode typeData);


    public Page<T> findAllByParentAndType(UUID parentid, String type, Pageable pageable);

}
