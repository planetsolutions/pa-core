package ru.doccloud.service;


import ru.doccloud.service.document.dto.GroupDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.document.dto.UserDTO;

import java.util.List;

/**
 * Created by Illia_Ushakov on 6/28/2017.
 */
public interface UserService {
    public UserDTO getUserDto(final String login);

    public List<GroupDTO> getGroups(String query);

	public List<UserDTO> getUsers(String query);

	public UserDTO updateUser(UserDTO dto);

	public GroupDTO updateGroup(GroupDTO dto);

	public GroupDTO getGroup(String id);

	public GroupDTO addGroup(GroupDTO dto);
}
