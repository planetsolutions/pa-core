package ru.doccloud.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateSetMoreStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import ru.doccloud.document.jooq.db.tables.records.DocumentsRecord;
import ru.doccloud.document.jooq.db.tables.records.GroupsRecord;
import ru.doccloud.document.jooq.db.tables.records.SystemRecord;
import ru.doccloud.document.jooq.db.tables.records.UserRolesRecord;
import ru.doccloud.document.jooq.db.tables.records.UsersRecord;
import ru.doccloud.document.model.Document;
import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.SystemDocument;
import ru.doccloud.document.model.User;
import ru.doccloud.document.model.UserRole;
import ru.doccloud.repository.UserRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.doccloud.document.jooq.db.tables.Documents.DOCUMENTS;
import static ru.doccloud.document.jooq.db.tables.Groups.GROUPS;
import static ru.doccloud.document.jooq.db.tables.System.SYSTEM;
import static ru.doccloud.document.jooq.db.tables.UserRoles.USER_ROLES;
import static ru.doccloud.document.jooq.db.tables.Users.USERS;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private  final DSLContext jooq;

    @Autowired
    public UserRepositoryImpl(DSLContext jooq) {
        this.jooq = jooq;
    }
    @Transactional(readOnly = true)
    @Override
    public List<Group> getGroups(String query){ 
    	Condition cond = null;
    	if (query!=null && query!="") cond = GROUPS.TITLE.startsWith(query);
        
    	SelectWhereStep<GroupsRecord> s = jooq.selectFrom(GROUPS);
    	if (cond!=null) s.where(cond);
    	final List<GroupsRecord> queryResult = s.fetchInto(GroupsRecord.class);
    	final List<Group> groups = convertGroupsQueryResultToModelObj(queryResult);
    	return groups;
	}
    
    @Transactional(readOnly = true)
    @Override
    public List<User> getUsers(String query){ 
    	Condition cond = null;
    	if (query!=null && query!="") cond = USERS.USERNAME.startsWith(query).or(USERS.FULLNAME.startsWith(query));
        
    	SelectWhereStep<UsersRecord> s = jooq.selectFrom(USERS);
    	if (cond!=null) s.where(cond);
    	final List<UsersRecord> queryResult = s.fetchInto(UsersRecord.class);
    	final List<User> users = convertUsersQueryResultToModelObj(queryResult);
    	return users;
	}
    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "userByLogin", cacheManager = "springCM")
    public User getUser(final String login) {
        LOGGER.trace("entering getUser(login = {})", login);

        final UsersRecord queryResult = jooq.selectFrom(USERS)
                .where(USERS.USERID.equal(login))
                .fetchOne();

        LOGGER.trace("getUser(): found user {}", queryResult);

//        todo rewrite using join
        final List<UserRolesRecord> userRolesQueryResult = jooq.selectFrom(USER_ROLES).where(USER_ROLES.USERID.equal(login)).fetchInto(UserRolesRecord.class);

        LOGGER.trace("getUser(): found roles {}", userRolesQueryResult);


        final User user = convertQueryResultToModelObject(queryResult, userRolesQueryResult);
//        todo add userRoles List to user Object
        LOGGER.trace("getUser(): found user {}", user);
        return user;
    }
    
    @Transactional
    @Override
    public User updateUser(User user) {
    	
		try {
			ObjectMapper mapper = new ObjectMapper();
	        UpdateSetMoreStep<UsersRecord> s = jooq.update(USERS)
			        .set(USERS.DETAILS, mapper.writeValueAsString(user.getDetails()));
	        if (user.getGroups()!=null){
	        	List<String> uGroups = new ArrayList<String>(Arrays.asList(user.getGroups()));
	        	uGroups.add(user.getUserId());
	        	s.set(USERS.GROUPS, uGroups.toArray(new String[0]));
	        }
			int updatedRecordCount = s.where(USERS.USERID.equal(user.getUserId())).execute();

			return getUser(user.getUserId());
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
			return null;
		}
    }
    
	@Override
	public Group updateGroup(Group group) {
        UpdateSetMoreStep<GroupsRecord> s = jooq.update(GROUPS)
		        .set(GROUPS.TITLE, group.getTitle());
		int updatedRecordCount = s.where(GROUPS.ID.equal(group.getId())).execute();

		return getGroup(group.getId());
	}
	@Override
	public Group getGroup(String id) {
		final GroupsRecord queryResult = jooq.selectFrom(GROUPS)
                .where(GROUPS.ID.equal(id))
                .fetchOne();
		return convertGroupToModelObject(queryResult);
		
	}

	@Override
	public Group addGroup(Group group) {
		GroupsRecord persisted = jooq.insertInto(
                GROUPS, GROUPS.ID, GROUPS.TITLE)
                .values(
                        group.getId(),group.getTitle())
                .returning()
                .fetchOne();
        Group returned = convertGroupToModelObject(persisted);
		return returned;
	}

    private static User convertQueryResultToModelObject(UsersRecord queryResult,  List<UserRolesRecord> userRolesQueryResult) {
    	ru.doccloud.document.model.User.Builder ubuilder = User.getBuilder(queryResult.getUserid())
		        //.password(queryResult.getPassword())
		        .fullName(queryResult.getFullname())
		        .email(queryResult.getEmail())
		        .groups(queryResult.getGroups());
    	if (queryResult.getDetails()!=null){
	    	try {
	    		ObjectMapper mapper = new ObjectMapper();
	        	JsonNode det = mapper.readTree(queryResult.getDetails());
				ubuilder.details(det);
			} catch (JsonProcessingException e) {
				LOGGER.error("getUser(): JsonProcessingException {}", e);
			} catch (IOException e) {
				LOGGER.error("getUser(): IOException {}", e);
			}
    	}
        
    	User user= ubuilder.build();
        user.setUserRoleList(convertUserRolesQueryResultToModelObj(userRolesQueryResult));
        return user;
    }

    private static List<UserRole> convertUserRolesQueryResultToModelObj(List<UserRolesRecord> userRolesQueryResult){
        if(userRolesQueryResult == null)
            return null;
        List<UserRole> userRoles = new ArrayList<>();

        for (UserRolesRecord queryResult : userRolesQueryResult) {
            UserRole userRole = UserRole.getBuilder(queryResult.getUserid()).role(queryResult.getRole()).build();
            userRoles.add(userRole);
        }

        return userRoles;
    }
    
    private static List<Group> convertGroupsQueryResultToModelObj(List<GroupsRecord> groupsQueryResult){
        if(groupsQueryResult == null)
            return null;
        List<Group> groups = new ArrayList<>();

        for (GroupsRecord queryResult : groupsQueryResult) {
        	Group group = Group.getBuilder(queryResult.getId()).title(queryResult.getTitle()).build();
        	groups.add(group);
        }

        return groups;
    }
	private Group convertGroupToModelObject(GroupsRecord queryResult) {
		return Group.getBuilder(queryResult.getId()).title(queryResult.getTitle()).build();
	}
    private static List<User> convertUsersQueryResultToModelObj(List<UsersRecord> UsersQueryResult){
        if(UsersQueryResult == null)
            return null;
        List<User> users = new ArrayList<>();

        for (UsersRecord queryResult : UsersQueryResult) {
        	ru.doccloud.document.model.User.Builder ubuilder = User.getBuilder(queryResult.getUserid())
        			.fullName(queryResult.getFullname())
        			.email(queryResult.getEmail())
        			.groups(queryResult.getGroups());
        			
			if (queryResult.getDetails()!=null){
		    	try {
		    		ObjectMapper mapper = new ObjectMapper();
		        	JsonNode det = mapper.readTree(queryResult.getDetails());
					ubuilder.details(det);
				} catch (JsonProcessingException e) {
					LOGGER.error("getUser(): JsonProcessingException {}", e);
				} catch (IOException e) {
					LOGGER.error("getUser(): IOException {}", e);
				}
	    	}
        	User user = ubuilder.build();
        	users.add(user);
        }

        return users;
    }

}
