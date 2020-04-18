package ru.doccloud.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.User;
import ru.doccloud.document.model.UserRole;
import ru.doccloud.repository.UserRepository;
import ru.doccloud.service.UserService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.GroupDTO;
import ru.doccloud.service.document.dto.UserDTO;
import ru.doccloud.service.document.dto.UserRoleDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemDocumentCrudService.class);

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<GroupDTO> getGroups(String query){
    	final List<Group> groups = userRepository.getGroups(query);
    	if(groups == null)
            return null;
        List<GroupDTO> groupsDTOList = new ArrayList<>();
        for (Group group: groups){
        	groupsDTOList.add(new GroupDTO(group.getTitle(),group.getId()));
        }
        return groupsDTOList;
    }
    
	@Override
	public GroupDTO updateGroup(GroupDTO dto) {
		final Group group =  userRepository.updateGroup(createModel(dto));
		return toGroupDto(group);
	}

	@Override
	public GroupDTO getGroup(String id) {
		final Group group =  userRepository.getGroup(id);
		return toGroupDto(group);
	}

	@Override
	public GroupDTO addGroup(GroupDTO dto) {
		final Group group =  userRepository.addGroup(createModel(dto));
		return toGroupDto(group);
	}

	@Override
	public List<UserDTO> getUsers(String query) {
		final List<User> users = userRepository.getUsers(query);
    	if(users == null)
            return null;
        List<UserDTO> usersDTOList = new ArrayList<>();
        for (User user: users){
        	usersDTOList.add(new UserDTO(user.getUserId(),"",user.getFullName(),user.getEmail(),user.getDetails(),user.getGroups(), null));
        }
        return usersDTOList;
	}

	@Override
	public UserDTO updateUser(UserDTO dto) {
		final User user =  userRepository.updateUser(createModel(dto));
		return toUserDto(user);
	}
	
    @Override
    public UserDTO getUserDto(final String login) {
        LOGGER.debug("entering getUserDto(login={})", login);
        final User user =  userRepository.getUser(login);

        LOGGER.debug("getUserDto(): found user {}", user);
        UserDTO userDTO =  toUserDto(user);

        LOGGER.debug("leaving getUserDto(): userDto {}", userDTO);
        return userDTO;
    }

    private UserDTO toUserDto(User user) { 
        return new UserDTO(user.getUserId(), user.getPassword(), user.getFullName(), user.getEmail(), user.getDetails(), user.getGroups(), convertUserRolesToDTOist(user.getUserRoleList()));
    } 
    private GroupDTO toGroupDto(Group group) { 
        return new GroupDTO(group.getTitle(), group.getId());
    } 
    private User createModel(UserDTO dto) {
        return User.getBuilder(dto.getUserId())
        		.details(dto.getDetails())
        		.email(dto.getEmail())
        		.fullName(dto.getFullName())
        		.groups(dto.getGroups())
                .build();
    }
    
    private Group createModel(GroupDTO dto) {
        return Group.getBuilder(dto.getId())
        		.title(dto.getTitle())
                .build();
    }
    private List<UserRoleDTO> convertUserRolesToDTOist(List<UserRole> userRoles) {
        if(userRoles == null)
            return null;
        List<UserRoleDTO> userRoleDTOList = new ArrayList<>();
        for (UserRole userRole: userRoles){
            userRoleDTOList.add(new UserRoleDTO(userRole.getRole(), userRole.getUserId()));
        }
        return userRoleDTOList;
    }



}
