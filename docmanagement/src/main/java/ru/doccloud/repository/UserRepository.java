package ru.doccloud.repository;

import ru.doccloud.document.model.Group;
import ru.doccloud.document.model.User;

import java.util.List;

public interface UserRepository {
    public User getUser(final String login);

    public List<Group> getGroups(String query);

    public List<User> getUsers(String query);

    public User updateUser(User user);

	public Group updateGroup(Group createModel);

	public Group getGroup(String id);

	public Group addGroup(Group createModel);
}
