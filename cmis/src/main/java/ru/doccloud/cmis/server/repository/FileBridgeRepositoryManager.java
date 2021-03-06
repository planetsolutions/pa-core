/*
 * Copyright 2014 Florian Müller & Jay Brown
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This code is based on the Apache Chemistry OpenCMIS FileShare project
 * <http://chemistry.apache.org/java/developing/repositories/dev-repositories-fileshare.html>.
 *
 * It is part of a training exercise and not intended for production use!
 *
 */
package ru.doccloud.cmis.server.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all repositories.
 */
@Component
public class FileBridgeRepositoryManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeRepositoryManager.class);
    private final Map<String, FileBridgeRepository> repositories;

    public FileBridgeRepositoryManager() {
        repositories = new ConcurrentHashMap<>();
    }

    /**
     * Adds a repository object.
     */
    public void addRepository(FileBridgeRepository fsr) {
        LOGGER.debug("entering addRepository(fsr={})", fsr);
        if (fsr == null || fsr.getRepositoryId() == null) {
            return;
        }

        repositories.put(fsr.getRepositoryId(), fsr);
        LOGGER.debug("leaving addRepository(): repositoryID {} has been added to repository map", fsr.getRepositoryId());
    }

    /**
     * Gets a repository object by id.
     */
    public FileBridgeRepository getRepository(String repositoryId) {
        LOGGER.debug("entering getRepository(repositoryId={})", repositoryId);
        FileBridgeRepository result = repositories.get(repositoryId);
        LOGGER.debug("leaving getRepository(): FileBridgeRepository was found {}", result);
        return result;
    }

    /**
     * Returns all repository objects.
     */
    public Collection<FileBridgeRepository> getRepositories() {
        return repositories.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (FileBridgeRepository repository : repositories.values()) {
            sb.append('[');
            sb.append(repository.getRepositoryId());
            sb.append(" -> ");
            sb.append(repository.getRootDirectory().getAbsolutePath());
            sb.append(']');
        }

        return sb.toString();
    }
}
