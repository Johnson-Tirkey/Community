package com.logicaldoc.web.service;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.logicaldoc.core.PersistenceException;
import com.logicaldoc.core.automation.Automation;
import com.logicaldoc.core.communication.EMail;
import com.logicaldoc.core.communication.EMailSender;
import com.logicaldoc.core.communication.Message;
import com.logicaldoc.core.communication.Recipient;
import com.logicaldoc.core.communication.SystemMessageDAO;
import com.logicaldoc.core.document.AbstractDocument;
import com.logicaldoc.core.document.dao.DocumentDAO;
import com.logicaldoc.core.generic.Generic;
import com.logicaldoc.core.generic.GenericDAO;
import com.logicaldoc.core.security.Device;
import com.logicaldoc.core.security.Geolocation;
import com.logicaldoc.core.security.Group;
import com.logicaldoc.core.security.LoginThrottle;
import com.logicaldoc.core.security.Menu;
import com.logicaldoc.core.security.MenuGroup;
import com.logicaldoc.core.security.Permission;
import com.logicaldoc.core.security.Session;
import com.logicaldoc.core.security.SessionManager;
import com.logicaldoc.core.security.Tenant;
import com.logicaldoc.core.security.User;
import com.logicaldoc.core.security.UserEvent;
import com.logicaldoc.core.security.UserHistory;
import com.logicaldoc.core.security.WorkingTime;
import com.logicaldoc.core.security.authentication.PasswordAlreadyUsedException;
import com.logicaldoc.core.security.authentication.PasswordWeakException;
import com.logicaldoc.core.security.authorization.PermissionException;
import com.logicaldoc.core.security.dao.DeviceDAO;
import com.logicaldoc.core.security.dao.GroupDAO;
import com.logicaldoc.core.security.dao.MenuDAO;
import com.logicaldoc.core.security.dao.TenantDAO;
import com.logicaldoc.core.security.dao.UserDAO;
import com.logicaldoc.core.sequence.Sequence;
import com.logicaldoc.core.sequence.SequenceDAO;
import com.logicaldoc.core.util.UserUtil;
import com.logicaldoc.gui.common.client.AccessDeniedException;
import com.logicaldoc.gui.common.client.InvalidSessionServerException;
import com.logicaldoc.gui.common.client.ServerException;
import com.logicaldoc.gui.common.client.beans.GUIDashlet;
import com.logicaldoc.gui.common.client.beans.GUIGroup;
import com.logicaldoc.gui.common.client.beans.GUIInfo;
import com.logicaldoc.gui.common.client.beans.GUIMenu;
import com.logicaldoc.gui.common.client.beans.GUIRight;
import com.logicaldoc.gui.common.client.beans.GUISecuritySettings;
import com.logicaldoc.gui.common.client.beans.GUISequence;
import com.logicaldoc.gui.common.client.beans.GUISession;
import com.logicaldoc.gui.common.client.beans.GUITenant;
import com.logicaldoc.gui.common.client.beans.GUIUser;
import com.logicaldoc.gui.common.client.beans.GUIValue;
import com.logicaldoc.gui.common.client.beans.GUIWorkingTime;
import com.logicaldoc.gui.common.client.services.SecurityService;
import com.logicaldoc.i18n.I18N;
import com.logicaldoc.util.Context;
import com.logicaldoc.util.LocaleUtil;
import com.logicaldoc.util.config.ContextProperties;
import com.logicaldoc.util.config.SecurityConfigurator;
import com.logicaldoc.util.config.WebConfigurator;
import com.logicaldoc.util.config.WebContextConfigurator;
import com.logicaldoc.util.crypt.CryptUtil;
import com.logicaldoc.util.security.PasswordGenerator;
import com.logicaldoc.util.sql.SqlUtil;
import com.logicaldoc.web.UploadServlet;

/**
 * Implementation of the SecurityService
 * 
 * @author Marco Meschieri - LogicalDOC
 * @since 6.0
 */
public class SecurityServiceImpl extends AbstractRemoteService implements SecurityService {

	private static final String SECURITY_GEOLOCATION_APIKEY = "security.geolocation.apikey";

	private static final String SSL_REQUIRED = "ssl.required";

	private static final String COOKIES_SECURE = "cookies.secure";

	private static final String COOKIES_SAMESITE = "cookies.samesite";

	private static final String ANONYMOUS_USER = ".anonymous.user";

	private static final String ANONYMOUS_KEY = ".anonymous.key";

	private static final String ANONYMOUS_ENABLED = ".anonymous.enabled";

	private static final String GUI_SAVELOGIN = ".gui.savelogin";

	private static final String PASSWORD_OCCURRENCE = ".password.occurrence";

	private static final String PASSWORD_SEQUENCE = ".password.sequence";

	private static final String PASSWORD_SPECIAL = ".password.special";

	private static final String PASSWORD_DIGIT = ".password.digit";

	private static final String PASSWORD_LOWERCASE = ".password.lowercase";

	private static final String PASSWORD_UPPERCASE = ".password.uppercase";

	private static final String ADMIN = "admin";

	private static final String PASSWORD_SIZE = ".password.size";

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(SecurityServiceImpl.class);

	public static GUITenant getTenant(long tenantId) {
		TenantDAO dao = (TenantDAO) Context.get().getBean(TenantDAO.class);
		Tenant tenant = null;
		try {
			tenant = dao.findById(tenantId);
			return fromTenant(tenant);
		} catch (PersistenceException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static GUITenant fromTenant(Tenant tenant) {
		if (tenant == null)
			return null;
		GUITenant ten = new GUITenant();
		ten.setId(tenant.getId());
		ten.setTenantId(tenant.getTenantId());
		ten.setCity(tenant.getCity());
		ten.setCountry(tenant.getCountry());
		ten.setDisplayName(tenant.getDisplayName());
		ten.setEmail(tenant.getEmail());
		ten.setName(tenant.getName());
		ten.setPostalCode(tenant.getPostalCode());
		ten.setState(tenant.getState());
		ten.setStreet(tenant.getStreet());
		ten.setTelephone(tenant.getTelephone());
		ten.setMaxRepoDocs(tenant.getMaxRepoDocs());
		ten.setMaxRepoSize(tenant.getMaxRepoSize());
		ten.setMaxSessions(tenant.getMaxSessions());
		ten.setQuotaThreshold(tenant.getQuotaThreshold());
		ten.setQuotaAlertRecipients(tenant.getQuotaAlertRecipientsAsList().toArray(new String[0]));
		ten.setMaxUsers(tenant.getMaxUsers());
		ten.setMaxGuests(tenant.getMaxGuests());
		ten.setEnabled(tenant.getEnabled() == 1);
		ten.setExpire(tenant.getExpire());

		return ten;
	}

	public static GUITenant getTenant(String tenantName) {
		TenantDAO dao = (TenantDAO) Context.get().getBean(TenantDAO.class);
		Tenant tenant = dao.findByName(tenantName);
		return fromTenant(tenant);
	}

	/**
	 * Used internally by login procedures, instantiates a new GUISession by a
	 * given authenticated user
	 * 
	 * @param sess the current session
	 * @param locale the current locale
	 * 
	 * @return session details
	 * 
	 * @throws ServerException a generic error
	 */
	public GUISession loadSession(Session sess, String locale) throws ServerException {
		GUISession session = new GUISession();
		session.setSid(sess.getSid());

		DocumentDAO documentDao = (DocumentDAO) Context.get().getBean(DocumentDAO.class);
		SystemMessageDAO messageDao = (SystemMessageDAO) Context.get().getBean(SystemMessageDAO.class);
		SequenceDAO seqDao = (SequenceDAO) Context.get().getBean(SequenceDAO.class);

		User user = sess.getUser();

		GUIUser guiUser = getUser(user.getId());

		if (StringUtils.isEmpty(locale) || "null".equals(locale)) {
			guiUser.setLanguage(user.getLanguage());
			locale = user.getLanguage();
		} else {
			guiUser.setLanguage(locale);
		}

		GUIInfo info = new InfoServiceImpl().getInfo(locale, sess.getTenantName(), true);
		session.setInfo(info);

		guiUser.setPasswordExpired(false);
		guiUser.setLockedDocs(documentDao.findByLockUserAndStatus(user.getId(), AbstractDocument.DOC_LOCKED).size());
		guiUser.setCheckedOutDocs(
				documentDao.findByLockUserAndStatus(user.getId(), AbstractDocument.DOC_CHECKED_OUT).size());
		guiUser.setUnreadMessages(messageDao.getUnreadCount(user.getUsername(), Message.TYPE_SYSTEM));
		guiUser.setQuota(user.getQuota());
		guiUser.setQuotaCount(seqDao.getCurrentValue("userquota", user.getId(), user.getTenantId()));
		guiUser.setCertDN(user.getCertDN());
		guiUser.setCertExpire(user.getCertExpire());
		guiUser.setSecondFactor(user.getSecondFactor());

		session.setSid(sess.getSid());
		session.setUser(guiUser);
		session.setLoggedIn(true);

		MenuDAO mdao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		List<Long> menus = mdao.findMenuIdByUserId(sess.getUserId(), true);
		guiUser.setMenus(menus.toArray(new Long[0]));

		loadDashlets(guiUser);

		/*
		 * Prepare an incoming message, if any
		 */
		GenericDAO gDao = (GenericDAO) Context.get().getBean(GenericDAO.class);
		Generic welcome = gDao.findByAlternateKey("guisetting", "gui.welcome", 0L, sess.getTenantId());
		if (welcome != null && StringUtils.isNotEmpty(welcome.getString1())) {
			Map<String, Object> dictionary = new HashMap<>();
			dictionary.put(Automation.LOCALE, user.getLocale());
			dictionary.put(Automation.TENANT_ID, sess.getTenantId());
			dictionary.put("session", sess);
			dictionary.put("user", session.getUser());

			Automation automation = new Automation("incomingmessage");
			String welcomeMessage = automation.evaluate(welcome.getString1(), dictionary);
			session.setWelcomeMessage(welcomeMessage != null ? welcomeMessage.trim() : null);
		}

		// Define the current locale
		sess.getDictionary().put(LOCALE, user.getLocale());
		sess.getDictionary().put(USER, user);

		ContextProperties config = Context.get().getProperties();
		guiUser.setPasswordMinLenght(Integer.parseInt(config.getProperty(sess.getTenantName() + PASSWORD_SIZE)));

		return session;
	}

	@Override
	public GUISession getSession(String locale, String sid) {
		try {
			Session sess = null;
			if (StringUtils.isEmpty(sid))
				sess = validateSession(getThreadLocalRequest());
			else
				sess = validateSession(sid);
			return loadSession(sess, locale);
		} catch (ServerException e) {
			log.debug(e.getMessage());
			return null;
		}
	}

	@Override
	public void logout() {
		try {
			Session session = validateSession(getThreadLocalRequest());
			if (session == null)
				return;

			FileUtils.forceDelete(UserUtil.getUserResource(session.getUserId(), "temp"));
			log.info("User {} logged out and closed session {}", session.getUsername(), session.getSid());
			kill(session.getSid());
		} catch (InvalidSessionServerException | IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public GUIValue changePassword(Long requestorUserId, long userId, String oldPassword, String newPassword,
			boolean notify) {
		try {
			UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
			User user = userDao.findById(userId);
			if (user == null)
				throw new ServerException(String.format("User %s not found", userId));
			userDao.initialize(user);

			User currentUser = getSessionUser();

			/*
			 * A non admin user cannot change the password of other users
			 */
			MenuDAO mDao = (MenuDAO) Context.get().getBean(MenuDAO.class);
			if (currentUser != null && currentUser.getId() != userId
					&& !mDao.isReadEnable(Menu.SECURITY, currentUser.getId()))
				throw new PermissionException(String.format("User %s not allowed to change the password of user %s",
						currentUser.getUsername(), user.getUsername()));

			if (oldPassword != null && !CryptUtil.cryptString(oldPassword).equals(user.getPassword()))
				throw new ServerException("Wrong old passord");

			UserHistory history = null;
			// The password was changed
			user.setDecodedPassword(newPassword);
			user.setPasswordChanged(new Date());
			user.setRepass("");

			// Add a user history entry
			history = new UserHistory();
			history.setUser(user);
			history.setEvent(UserEvent.PASSWORDCHANGED.toString());
			history.setComment("");

			/*
			 * If the credentials must be notified, we have to mark the password
			 * as expired for security reasons, so the user will change it at
			 * first login.
			 */
			user.setPasswordExpired(notify || requestorUserId == null || !requestorUserId.equals(userId) ? 1 : 0);

			userDao.store(user, history);
			if (notify)
				notifyAccount(user, newPassword);

			return new GUIValue("0", null);
		} catch (PasswordWeakException e) {
			log.error(e.getMessage(), e);
			return new GUIValue("4", e.getMessages().stream().collect(Collectors.joining("\n")));
		} catch (PasswordAlreadyUsedException e) {
			log.error(e.getMessage(), e);
			return new GUIValue("3", e.getFormattedDate());
		} catch (MessagingException e) {
			log.warn(e.getMessage(), e);
			return new GUIValue("2", null);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new GUIValue("1", null);
		}
	}

	private User getSessionUser() {
		User currentUser = null;
		try {
			currentUser = getSessionUser(getThreadLocalRequest());
		} catch (Exception e) {
			// Do nothing
		}
		return currentUser;
	}

	@Override
	public void addUserToGroup(long groupId, long userId) throws ServerException {
		checkMenu(getThreadLocalRequest(), Menu.SECURITY);
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);

		try {
			User user = userDao.findById(userId, true);
			user.addGroup(groupDao.findById(groupId));
			userDao.store(user);
			userDao.initialize(user);
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public void deleteGroup(long groupId) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
		try {
			Group grp = groupDao.findById(groupId);
			groupDao.initialize(grp);
			for (User user : grp.getUsers()) {
				user.removeGroup(groupId);
				userDao.store(user);
			}
			groupDao.delete(groupId);
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public void deleteUser(long userId) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);
		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);

		// Create the user history event
		UserHistory transaction = new UserHistory();
		transaction.setSession(session);
		transaction.setEvent(UserEvent.DELETED.toString());
		transaction.setComment("");
		try {
			transaction.setUser(userDao.findById(userId));
			userDao.delete(userId, transaction);
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public GUIGroup getGroup(long groupId) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		validateSession(getThreadLocalRequest());
		GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
		try {
			Group group = groupDao.findById(groupId);

			if (group != null) {
				GUIGroup grp = new GUIGroup();
				grp.setId(groupId);
				grp.setDescription(group.getDescription());
				grp.setName(group.getName());
				grp.setSource(group.getSource());
				return grp;
			}

			return null;
		} catch (PersistenceException e) {
			return (GUIGroup) throwServerException(session, log, e);
		}
	}

	@Override
	public GUIUser getUser(long userId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		SequenceDAO seqDao = (SequenceDAO) Context.get().getBean(SequenceDAO.class);

		try {
			User user = userDao.findById(userId);
			if (user != null) {
				userDao.initialize(user);

				GUIUser guiUser = new GUIUser();
				guiUser.setId(userId);
				guiUser.setTenant(getTenant(user.getTenantId()));
				guiUser.setAddress(user.getStreet());
				guiUser.setCell(user.getTelephone2());
				guiUser.setPhone(user.getTelephone());
				guiUser.setCity(user.getCity());
				guiUser.setCountry(user.getCountry());
				guiUser.setEmail(user.getEmail());
				guiUser.setEmail2(user.getEmail2());
				guiUser.setEnabled(user.getEnabled() == 1);
				guiUser.setFirstName(user.getFirstName());
				guiUser.setLanguage(user.getLanguage());
				guiUser.setName(user.getName());
				guiUser.setPostalCode(user.getPostalcode());
				guiUser.setState(user.getState());
				guiUser.setUsername(user.getUsername());
				guiUser.setPasswordExpires(user.getPasswordExpires() == 1);
				guiUser.setPasswordExpired(user.getPasswordExpired() == 1);
				guiUser.setWelcomeScreen(user.getWelcomeScreen());
				guiUser.setDefaultWorkspace(user.getDefaultWorkspace());
				guiUser.setIpWhitelist(user.getIpWhiteList());
				guiUser.setIpBlacklist(user.getIpBlackList());
				guiUser.setEmailSignature(user.getEmailSignature());
				guiUser.setEmailSignature2(user.getEmailSignature2());
				guiUser.setCertExpire(user.getCertExpire());
				guiUser.setCertDN(user.getCertDN());
				guiUser.setSecondFactor(user.getSecondFactor());
				guiUser.setKey(user.getKey());
				guiUser.setType(user.getType());
				guiUser.setDocsGrid(user.getDocsGrid());
				guiUser.setHitsGrid(user.getHitsGrid());
				guiUser.setDateFormat(user.getDateFormat());
				guiUser.setDateFormatShort(user.getDateFormatShort());
				guiUser.setDateFormatLong(user.getDateFormatLong());
				guiUser.setSearchPref(user.getSearchPref());
				guiUser.setExpire(user.getExpire());
				guiUser.setEnforceWorkingTime(user.getEnforceWorkingTime() == 1);
				guiUser.setMaxInactivity(user.getMaxInactivity());
				guiUser.setTimeZone(user.getTimeZone());
				guiUser.setSource(user.getSource());

				GUIGroup[] grps = new GUIGroup[user.getGroups().size()];
				int i = 0;
				for (Group group : user.getGroups()) {
					grps[i] = new GUIGroup();
					grps[i].setId(group.getId());
					grps[i].setName(group.getName());
					grps[i].setDescription(group.getDescription());
					grps[i].setType(group.getType());
					grps[i].setSource(group.getSource());
					i++;
				}
				guiUser.setGroups(grps);

				guiUser.setQuota(user.getQuota());

				guiUser.setQuotaCount(seqDao.getCurrentValue("userquota", user.getId(), user.getTenantId()));

				guiUser.setTenant(getTenant(user.getTenantId()));

				ContextProperties config = Context.get().getProperties();
				guiUser.setPasswordMinLenght(
						Integer.parseInt(config.getProperty(guiUser.getTenant().getName() + PASSWORD_SIZE)));

				loadDashlets(guiUser);

				loadWorkingTimes(guiUser);

				guiUser.setCustomActions(getMenus(Menu.CUSTOM_ACTIONS, guiUser.getLanguage(), true));

				return guiUser;
			}
		} catch (NumberFormatException | PersistenceException | ServerException e) {
			throwServerException(session, log, e);
		}

		return null;
	}

	/**
	 * Retrieves the dashlets configuration
	 * 
	 * @param usr current user
	 */
	protected static void loadDashlets(GUIUser usr) {
		DashletServiceImpl dashletService = new DashletServiceImpl();
		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		List<GUIDashlet> dashlets = new ArrayList<>();
		Map<String, Generic> map = userDao.findUserSettings(usr.getId(), "dashlet");
		for (Generic generic : map.values()) {
			String name = generic.getSubtype().substring(generic.getSubtype().indexOf('-') + 1);

			try {
				GUIDashlet dashlet = dashletService.get(name);
				if (dashlet != null) {
					dashlet.setColumn(generic.getInteger2() != null ? generic.getInteger2().intValue() : 0);
					dashlet.setRow(generic.getInteger3() != null ? generic.getInteger3().intValue() : 0);
					dashlet.setIndex(generic.getString1() != null ? Integer.parseInt(generic.getString1()) : 0);
					dashlets.add(dashlet);
				}
			} catch (NumberFormatException | ServerException e) {
				// Nothing to do
			}
		}
		usr.setDashlets(dashlets.toArray(new GUIDashlet[0]));
	}

	@Override
	public void removeFromGroup(long groupId, long[] userIds) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.ADMINISTRATION);

		checkMenu(getThreadLocalRequest(), Menu.ADMINISTRATION);

		try {
			UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
			GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
			Group group = groupDao.findById(groupId);
			for (long id : userIds) {
				User user = userDao.findById(id, true);
				user.removeGroup(group.getId());
				userDao.store(user);
			}
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public GUIGroup saveGroup(GUIGroup group) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.ADMINISTRATION);

		try {
			GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
			Group grp;
			if (group.getId() != 0) {
				grp = groupDao.findById(group.getId());
				groupDao.initialize(grp);

				grp.setName(group.getName());
				grp.setDescription(group.getDescription());

				if (group.getInheritGroupId() == null || group.getInheritGroupId().longValue() == 0) {
					groupDao.store(grp);
				} else {
					groupDao.insert(grp, group.getInheritGroupId().longValue());
				}

			} else {
				grp = new Group();
				grp.setTenantId(session.getTenantId());
				grp.setName(group.getName());
				grp.setDescription(group.getDescription());

				groupDao.store(grp);

				if (group.getInheritGroupId() != null && group.getInheritGroupId().longValue() != 0)
					groupDao.inheritACLs(grp, group.getInheritGroupId().longValue());
			}

			group.setId(grp.getId());
			return group;
		} catch (PersistenceException e) {
			return (GUIGroup) throwServerException(session, log, e);
		}
	}

	@Override
	public GUIUser saveUser(GUIUser guiUser, GUIInfo info) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		boolean createNew = false;

		// Disallow the editing of other users if you do not have access to
		// the Security
		disallowEditingOfOtherUsers(guiUser, session);

		try {
			User user = getOrCreateUser(guiUser);

			createNew = user.getId() == 0L;

			user.setTenantId(session.getTenantId());
			user.setCity(guiUser.getCity());
			user.setCountry(guiUser.getCountry());
			user.setEmail(guiUser.getEmail());
			user.setEmail2(guiUser.getEmail2());
			user.setFirstName(guiUser.getFirstName());
			user.setName(guiUser.getName());
			user.setLanguage(guiUser.getLanguage());
			user.setPostalcode(guiUser.getPostalCode());
			user.setState(guiUser.getState());
			user.setStreet(guiUser.getAddress());
			user.setTelephone(guiUser.getPhone());
			user.setTelephone2(guiUser.getCell());
			user.setUsername(guiUser.getUsername());
			user.setEnabled(guiUser.isEnabled() ? 1 : 0);
			user.setPasswordExpires(guiUser.isPasswordExpires() ? 1 : 0);
			user.setPasswordExpired(guiUser.isPasswordExpired() ? 1 : 0);
			user.setWelcomeScreen(guiUser.getWelcomeScreen());
			user.setIpWhiteList(guiUser.getIpWhitelist());
			user.setIpBlackList(guiUser.getIpBlacklist());
			user.setEmailSignature(guiUser.getEmailSignature());
			user.setDefaultWorkspace(guiUser.getDefaultWorkspace());
			user.setQuota(guiUser.getQuota());
			user.setSecondFactor(StringUtils.isEmpty(guiUser.getSecondFactor()) ? null : guiUser.getSecondFactor());
			user.setKey(guiUser.getKey());
			user.setType(guiUser.getType());
			user.setDocsGrid(guiUser.getDocsGrid());
			user.setHitsGrid(guiUser.getHitsGrid());
			user.setDateFormat(guiUser.getDateFormat());
			user.setDateFormatShort(guiUser.getDateFormatShort());
			user.setDateFormatLong(guiUser.getDateFormatLong());
			user.setSearchPref(guiUser.getSearchPref());
			user.setEnforceWorkingTime(guiUser.isEnforceWorkingTime() ? 1 : 0);
			user.setSecondFactor(guiUser.getSecondFactor());
			user.setMaxInactivity(guiUser.getMaxInactivity() == null || guiUser.getMaxInactivity() == 0 ? null
					: guiUser.getMaxInactivity());
			user.setTimeZone(guiUser.getTimeZone());

			setExpire(user, guiUser);

			if (createNew) {
				User existingUser = userDao.findByUsername(guiUser.getUsername());
				if (existingUser != null) {
					log.warn("Tried to create duplicate username {}", guiUser.getUsername());
					guiUser.setWelcomeScreen(-99);
					return guiUser;
				}

				String tenant = session.getTenantName();

				// Generate an initial password(that must be changed)
				ContextProperties config = Context.get().getProperties();
				String password = PasswordGenerator.generate(config.getInt(tenant + PASSWORD_SIZE, 8),
						config.getInt(tenant + PASSWORD_UPPERCASE, 2), config.getInt(tenant + PASSWORD_LOWERCASE, 2),
						config.getInt(tenant + PASSWORD_DIGIT, 1), config.getInt(tenant + PASSWORD_SPECIAL, 1),
						config.getInt(tenant + PASSWORD_SEQUENCE, 4), config.getInt(tenant + PASSWORD_OCCURRENCE, 3));
				user.setDecodedPassword(password);
				user.setPasswordExpired(1);
				user.setPasswordChanged(new Date());
			}

			saveWorkingTimes(user, guiUser.getWorkingTimes());

			UserHistory transaction = new UserHistory();
			transaction.setSession(session);
			transaction.setEvent(UserEvent.UPDATED.toString());
			userDao.store(user, transaction);

			guiUser.setId(user.getId());

			setGroups(user, guiUser);

			// Notify the user by email
			if (createNew && guiUser.isNotifyCredentials())
				notifyAccount(user, user.getDecodedPassword());
		} catch (MessagingException me) {
			log.warn(me.getMessage(), me);
		} catch (Exception e) {
			return (GUIUser) throwServerException(session, log, e);
		}

		return getUser(guiUser.getId());
	}

	private void disallowEditingOfOtherUsers(GUIUser guiUser, Session session)
			throws InvalidSessionServerException, AccessDeniedException {
		if (guiUser.getId() != session.getUserId() && getThreadLocalRequest() != null)
			checkMenu(getThreadLocalRequest(), Menu.SECURITY);
	}

	private void setGroups(User user, GUIUser guiUser) throws PersistenceException {
		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		GroupDAO groupDao = (GroupDAO) Context.get().getBean(GroupDAO.class);
		user.removeGroupMemberships(null);
		long[] ids = new long[guiUser.getGroups().length];
		for (int i = 0; i < guiUser.getGroups().length; i++) {
			ids[i] = guiUser.getGroups()[i].getId();
			user.addGroup(groupDao.findById(ids[i]));
		}

		Group adminGroup = groupDao.findByName(ADMIN, user.getTenantId());
		groupDao.initialize(adminGroup);

		// The admin user must be always member of admin group
		if (ADMIN.equals(guiUser.getUsername()) && !guiUser.isMemberOf(Group.GROUP_ADMIN))
			user.addGroup(adminGroup);

		userDao.store(user);
	}

	private void setExpire(User usr, GUIUser guiUser) {
		if (guiUser.getExpire() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(guiUser.getExpire());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 0);
			usr.setExpire(cal.getTime());
		} else
			usr.setExpire(null);
	}

	private User getOrCreateUser(GUIUser guiUser) throws PersistenceException {
		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		User usr;
		if (guiUser.getId() != 0) {
			usr = userDao.findById(guiUser.getId());
			userDao.initialize(usr);
		} else {
			usr = new User();
		}
		return usr;
	}

	private void saveWorkingTimes(User user, GUIWorkingTime[] guiWts) {
		if (user.getWorkingTimes() != null)
			user.getWorkingTimes().clear();
		if (guiWts == null || guiWts.length < 1)
			return;

		Calendar cal = Calendar.getInstance();
		for (GUIWorkingTime guiWorkingTime : guiWts) {
			cal.setTime(guiWorkingTime.getStart());
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			int hourStart = cal.get(Calendar.HOUR_OF_DAY);
			int minuteStart = cal.get(Calendar.MINUTE);
			WorkingTime wt = new WorkingTime(dayOfWeek, hourStart, minuteStart);

			cal.setTime(guiWorkingTime.getEnd());
			wt.setHourEnd(cal.get(Calendar.HOUR_OF_DAY));
			wt.setMinuteEnd(cal.get(Calendar.MINUTE));

			wt.setLabel(guiWorkingTime.getLabel());
			wt.setDescription(guiWorkingTime.getDescription());

			user.getWorkingTimes().add(wt);
		}
	}

	private void loadWorkingTimes(GUIUser guiUser) throws PersistenceException {
		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		User user = userDao.findById(guiUser.getId());
		if (user == null)
			return;
		else
			userDao.initialize(user);

		List<GUIWorkingTime> guiWts = new ArrayList<>();
		if (user.getWorkingTimes() != null)
			for (WorkingTime workingTime : user.getWorkingTimes()) {
				int dayOfWeek = workingTime.getDayOfWeek();

				Calendar cal = Calendar.getInstance();
				// Set the calendar to the last Sunday
				cal.add(Calendar.DAY_OF_WEEK, -(cal.get(Calendar.DAY_OF_WEEK) - 1));

				// Now shift the day of week of the working time
				cal.add(Calendar.DAY_OF_WEEK, dayOfWeek - 1);
				cal.set(Calendar.HOUR_OF_DAY, workingTime.getHourStart());
				cal.set(Calendar.MINUTE, workingTime.getMinuteStart());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 99);
				Date start = cal.getTime();

				cal.set(Calendar.HOUR_OF_DAY, workingTime.getHourEnd());
				cal.set(Calendar.MINUTE, workingTime.getMinuteEnd());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 99);
				Date end = cal.getTime();

				GUIWorkingTime guiWt = new GUIWorkingTime(workingTime.getLabel(), start, end);
				guiWt.setDescription(workingTime.getDescription());

				guiWts.add(guiWt);
			}

		guiUser.setWorkingTimes(guiWts.toArray(new GUIWorkingTime[0]));
	}

	/**
	 * Notify the user with it's new account
	 * 
	 * @param user The created user
	 * @param password The decoded password
	 * 
	 * @throws MessagingException Cannot notify user
	 */
	private void notifyAccount(User user, String password) throws MessagingException {
		EMail email;
		email = new EMail();
		email.setHtml(1);
		Recipient recipient = new Recipient();
		recipient.setAddress(user.getEmail());
		recipient.setRead(1);
		email.addRecipient(recipient);
		email.setFolder("outbox");
		email.setUsername(user.getUsername());
		email.setSentDate(new Date());

		Locale locale = user.getLocale();
		email.setLocale(locale);

		/*
		 * Prepare the template
		 */
		Map<String, Object> dictionary = new HashMap<>();
		ContextProperties config = Context.get().getProperties();
		String address = config.getProperty("server.url");
		dictionary.put("url", address);
		dictionary.put("user", user);
		dictionary.put("password", password);
		dictionary.put(Automation.LOCALE, locale);

		EMailSender sender = new EMailSender(user.getTenantId());
		sender.send(email, "psw.rec1", dictionary);
	}

	@Override
	public GUIUser saveProfile(GUIUser user) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);

		// Disallow the editing of other users if you do not have access to
		// the Security
		if (user.getId() != session.getUserId())
			checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		try {
			User usr = userDao.findById(user.getId());
			userDao.initialize(usr);

			usr.setFirstName(user.getFirstName());
			usr.setName(user.getName());
			usr.setEmail(user.getEmail());
			usr.setEmail2(user.getEmail2());
			usr.setLanguage(user.getLanguage());
			usr.setStreet(user.getAddress());
			usr.setPostalcode(user.getPostalCode());
			usr.setCity(user.getCity());
			usr.setCountry(user.getCountry());
			usr.setState(user.getState());
			usr.setTelephone(user.getPhone());
			usr.setTelephone2(user.getCell());
			usr.setWelcomeScreen(user.getWelcomeScreen());
			usr.setDefaultWorkspace(user.getDefaultWorkspace());
			usr.setEmailSignature(user.getEmailSignature());
			usr.setEmailSignature2(user.getEmailSignature2());
			usr.setTimeZone(user.getTimeZone());
			usr.setDocsGrid(user.getDocsGrid());
			usr.setHitsGrid(user.getHitsGrid());

			usr.setDateFormat(user.getDateFormat());
			usr.setDateFormatShort(user.getDateFormatShort());
			usr.setDateFormatLong(user.getDateFormatLong());
			usr.setSearchPref(user.getSearchPref());

			UserHistory transaction = new UserHistory();
			transaction.setSession(session);
			transaction.setEvent(UserEvent.UPDATED.toString());
			userDao.store(usr, transaction);

			return user;
		} catch (PersistenceException e) {
			return (GUIUser) throwServerException(session, log, e);
		}
	}

	@Override
	public GUIUser saveInterfaceSettings(GUIUser user) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);

		// Disallow the editing of other users if you do not have access to
		// the Security
		if (user.getId() != session.getUserId())
			checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		try {
			User usr = userDao.findById(user.getId());
			userDao.initialize(usr);

			usr.setWelcomeScreen(user.getWelcomeScreen());
			usr.setDefaultWorkspace(user.getDefaultWorkspace());
			usr.setDocsGrid(user.getDocsGrid());
			usr.setHitsGrid(user.getHitsGrid());

			userDao.store(usr);
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}

		return user;
	}

	@Override
	public void kill(String sid) {
		// Kill the LogicalDOC session
		SessionManager.get().kill(sid);

		SessionManager.get().removeSid(getThreadLocalRequest());

		// Also kill the servlet container session, if any
		HttpSession httpSession = SessionManager.get().getServletSession(sid);
		if (httpSession != null)
			httpSession.invalidate();
	}

	@Override
	public GUISecuritySettings loadSettings() throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		GUISecuritySettings securitySettings = new GUISecuritySettings();

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		ContextProperties pbean = Context.get().getProperties();

		securitySettings.setPwdExpiration(pbean.getInt(session.getTenantName() + ".password.ttl", 90));
		securitySettings.setPwdSize(pbean.getInt(session.getTenantName() + PASSWORD_SIZE, 8));
		securitySettings.setPwdLowerCase(pbean.getInt(session.getTenantName() + PASSWORD_LOWERCASE, 2));
		securitySettings.setPwdUpperCase(pbean.getInt(session.getTenantName() + PASSWORD_UPPERCASE, 2));
		securitySettings.setPwdDigit(pbean.getInt(session.getTenantName() + PASSWORD_DIGIT, 1));
		securitySettings.setPwdSpecial(pbean.getInt(session.getTenantName() + PASSWORD_SPECIAL, 1));
		securitySettings.setPwdSequence(pbean.getInt(session.getTenantName() + PASSWORD_SEQUENCE, 3));
		securitySettings.setPwdOccurrence(pbean.getInt(session.getTenantName() + PASSWORD_OCCURRENCE, 3));
		securitySettings.setPwdEnforceHistory(pbean.getInt(session.getTenantName() + ".password.enforcehistory", 3));
		securitySettings.setMaxInactivity(pbean.getInt(session.getTenantName() + ".security.user.maxinactivity"));
		if (StringUtils.isNotEmpty(pbean.getProperty(session.getTenantName() + GUI_SAVELOGIN)))
			securitySettings.setSaveLogin("true".equals(pbean.getProperty(session.getTenantName() + GUI_SAVELOGIN)));
		securitySettings.setIgnoreLoginCase("true".equals(pbean.getProperty("login.ignorecase")));
		securitySettings.setAllowSidInRequest(pbean.getBoolean("security.acceptsid", false));
		if (StringUtils.isNotEmpty(pbean.getProperty(session.getTenantName() + ANONYMOUS_ENABLED)))
			securitySettings.setEnableAnonymousLogin(
					"true".equals(pbean.getProperty(session.getTenantName() + ANONYMOUS_ENABLED)));
		if (StringUtils.isNotEmpty(pbean.getProperty(session.getTenantName() + ANONYMOUS_KEY)))
			securitySettings.setAnonymousKey(pbean.getProperty(session.getTenantName() + ANONYMOUS_KEY));
		if (StringUtils.isNotEmpty(pbean.getProperty(session.getTenantName() + ANONYMOUS_USER))) {
			User user = userDao.findByUsername(pbean.getProperty(session.getTenantName() + ANONYMOUS_USER));
			if (user != null)
				securitySettings.setAnonymousUser(getUser(user.getId()));
		}
		if (StringUtils.isNotEmpty(pbean.getProperty(SSL_REQUIRED)))
			securitySettings.setForceSsl("true".equals(pbean.getProperty(SSL_REQUIRED)));
		if (StringUtils.isNotEmpty(pbean.getProperty(COOKIES_SECURE)))
			securitySettings.setCookiesSecure("true".equals(pbean.getProperty(COOKIES_SECURE)));
		securitySettings.setCookiesSameSite(pbean.getProperty(COOKIES_SAMESITE, "unset"));

		securitySettings.setAlertNewDevice(pbean.getBoolean(session.getTenantName() + ".alertnewdevice", true));

		securitySettings.setGeolocationEnabled(pbean.getBoolean("security.geolocation.enabled", true));
		securitySettings.setGeolocationCache(pbean.getBoolean("security.geolocation.cache", false));
		securitySettings.setGeolocationKey(pbean.getProperty(SECURITY_GEOLOCATION_APIKEY));
		securitySettings.setGeolocationDbVer(Geolocation.get().getDatabaseVersion());

		securitySettings.setContentSecurityPolicy(new SecurityConfigurator().getContentSecurityPolicy());

		log.debug("Security settings data loaded successfully.");

		return securitySettings;
	}

	@Override
	public boolean saveSettings(GUISecuritySettings settings) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		boolean restartRequired = false;

		ContextProperties conf = Context.get().getProperties();

		if (session.getTenantId() == Tenant.DEFAULT_ID) {
			conf.setProperty("login.ignorecase", Boolean.toString(settings.isIgnoreLoginCase()));
			conf.setProperty(SSL_REQUIRED, Boolean.toString(settings.isForceSsl()));
			conf.setProperty(COOKIES_SECURE, Boolean.toString(settings.isCookiesSecure()));
			conf.setProperty("security.acceptsid", Boolean.toString(settings.isAllowSidInRequest()));
			conf.setProperty("security.geolocation.enabled", Boolean.toString(settings.isGeolocationEnabled()));
			conf.setProperty("security.geolocation.cache", Boolean.toString(settings.isGeolocationCache()));
			conf.setProperty(SECURITY_GEOLOCATION_APIKEY,
					settings.getGeolocationKey() != null ? settings.getGeolocationKey() : "");

			try {
				// Update the WEB-INF/web.xml
				ServletContext context = getServletContext();
				String policy = "true".equals(conf.getProperty(SSL_REQUIRED)) ? "CONFIDENTIAL" : "NONE";
				WebConfigurator webConfigurator = new WebConfigurator(context.getRealPath("/WEB-INF/web.xml"));
				restartRequired = webConfigurator.setTransportGuarantee(policy);

				// Update the META-INF/context.xml
				conf.setProperty(COOKIES_SAMESITE, settings.getCookiesSameSite());
				WebContextConfigurator webContextConfigurator = new WebContextConfigurator(
						context.getRealPath("/META-INF/context.xml"));
				restartRequired = webContextConfigurator.setSameSiteCookies(settings.getCookiesSameSite());

				// Update the context-security.xml
				SecurityConfigurator secConfigurator = new SecurityConfigurator();
				restartRequired = restartRequired
						|| secConfigurator.setContentSecurityPolicy(settings.getContentSecurityPolicy());
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}

			// Invalidate then Geolocation
			Geolocation.get().dispose();
		}

		conf.setProperty(session.getTenantName() + ".password.ttl", Integer.toString(settings.getPwdExpiration()));
		conf.setProperty(session.getTenantName() + ".security.user.maxinactivity",
				settings.getMaxInactivity() == null || settings.getMaxInactivity().intValue() <= 0 ? ""
						: Integer.toString(settings.getMaxInactivity()));
		conf.setProperty(session.getTenantName() + ".password.enforcehistory",
				Integer.toString(settings.getPwdEnforceHistory()));
		conf.setProperty(session.getTenantName() + PASSWORD_SIZE, Integer.toString(settings.getPwdSize()));
		conf.setProperty(session.getTenantName() + PASSWORD_LOWERCASE, Integer.toString(settings.getPwdLowerCase()));
		conf.setProperty(session.getTenantName() + PASSWORD_UPPERCASE, Integer.toString(settings.getPwdUpperCase()));
		conf.setProperty(session.getTenantName() + PASSWORD_DIGIT, Integer.toString(settings.getPwdDigit()));
		conf.setProperty(session.getTenantName() + PASSWORD_SPECIAL, Integer.toString(settings.getPwdSpecial()));
		conf.setProperty(session.getTenantName() + PASSWORD_SEQUENCE, Integer.toString(settings.getPwdSequence()));
		conf.setProperty(session.getTenantName() + PASSWORD_OCCURRENCE, Integer.toString(settings.getPwdOccurrence()));
		conf.setProperty(session.getTenantName() + GUI_SAVELOGIN, Boolean.toString(settings.isSaveLogin()));
		conf.setProperty(session.getTenantName() + ".alertnewdevice", Boolean.toString(settings.isAlertNewDevice()));
		conf.setProperty(session.getTenantName() + ANONYMOUS_ENABLED,
				Boolean.toString(settings.isEnableAnonymousLogin()));
		conf.setProperty(session.getTenantName() + ANONYMOUS_KEY, settings.getAnonymousKey().trim());

		if (settings.getAnonymousUser() != null)
			conf.setProperty(session.getTenantName() + ANONYMOUS_USER, settings.getAnonymousUser().getUsername());

		try {
			conf.write();
			log.info("Security settings data written successfully.");
			return restartRequired;
		} catch (IOException e) {
			return (Boolean) throwServerException(session, log, e);
		}
	}

	private boolean saveRules(Session session, Menu menu, GUIRight[] rights)
			throws PermissionException, PersistenceException {
		MenuDAO mdao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		if (!mdao.isReadEnable(Menu.SECURITY, session.getUserId()))
			throw new PermissionException(session.getUsername(), "Menu " + menu.getName(), Permission.READ);

		GroupDAO gdao = (GroupDAO) Context.get().getBean(GroupDAO.class);

		boolean sqlerrors = false;

		mdao.initialize(menu);
		menu.setSecurityRef(null);

		// Remove all current tenant rights
		Set<MenuGroup> grps = new HashSet<>();
		for (MenuGroup mg : menu.getMenuGroups()) {
			Group group = gdao.findById(mg.getGroupId());
			if (group != null && group.getTenantId() != session.getTenantId())
				grps.add(mg);
		}
		menu.getMenuGroups().clear();

		sqlerrors = false;
		for (GUIRight right : rights) {
			Group group = gdao.findById(right.getEntityId());
			if (group == null || group.getTenantId() != session.getTenantId())
				continue;

			MenuGroup fg = null;
			if (right.isRead()) {
				fg = new MenuGroup();
				fg.setGroupId(right.getEntityId());
			}
			grps.add(fg);
		}

		menu.setMenuGroups(grps);
		try {
			mdao.store(menu);
		} catch (PersistenceException e) {
			sqlerrors = true;
		}

		return !sqlerrors;
	}

	@Override
	public void applyRights(GUIMenu menu) throws ServerException {
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);
		MenuDAO mdao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		try {
			saveRules(session, mdao.findById(menu.getId()), menu.getRights());
		} catch (PermissionException | PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public void deleteMenu(long menuId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		MenuDAO dao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		try {
			Menu menu = dao.findById(menuId);
			if (menu == null)
				throw new ServerException("Unexisting menu identified by " + menuId);
			if (menu.getType() == Menu.TYPE_DEFAULT)
				throw new PermissionException("Cannot delete legacy menu " + menuId);
			dao.delete(menuId);
		} catch (PermissionException | PersistenceException | ServerException e) {
			throwServerException(session, log, e);
		}

	}

	@Override
	public void saveMenus(GUIMenu[] menus, String locale) throws ServerException {
		validateSession(getThreadLocalRequest());

		if (menus == null || menus.length < 1)
			return;
		for (GUIMenu guiMenu : menus)
			saveMenu(guiMenu, locale);
	}

	@Override
	public GUIMenu saveMenu(GUIMenu guiMenu, String locale) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		MenuDAO dao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		try {
			Menu menu = new Menu();
			if (guiMenu.getId() != 0L) {
				menu = dao.findById(guiMenu.getId());
				dao.initialize(menu);
			} else {
				menu.setTenantId(session.getTenantId());
			}

			menu.setName(guiMenu.getName().replace("/", ""));
			menu.setAutomation(guiMenu.getAutomation());
			menu.setEnabled(guiMenu.isEnabled() ? 1 : 0);
			menu.setDescription(guiMenu.getDescription());
			menu.setPosition(guiMenu.getPosition());
			menu.setParentId(guiMenu.getParentId());
			menu.setRoutineId(guiMenu.getRoutineId());
			menu.setSecurityRef(guiMenu.getSecurityRef());
			menu.setType(guiMenu.getType());

			menu.getMenuGroups().clear();
			if (guiMenu.getRights() != null && guiMenu.getRights().length > 0) {
				for (GUIRight right : guiMenu.getRights())
					menu.getMenuGroups().add(new MenuGroup(right.getEntityId()));
			}

			dao.store(menu);
			return getMenu(menu.getId(), locale);
		} catch (PersistenceException | ServerException e) {
			return (GUIMenu) throwServerException(session, log, e);
		}

	}

	@Override
	public GUIMenu[] getMenus(long parentId, String locale, boolean enabledOnly) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		MenuDAO dao = (MenuDAO) Context.get().getBean(MenuDAO.class);

		List<Menu> menus = dao.findByUserId(session.getUserId(), parentId, enabledOnly);
		List<GUIMenu> guiMenus = menus.stream().filter(m -> m.getTenantId() == session.getTenantId()).map(m -> {
			try {
				return toGUIMenu(m, locale);
			} catch (PersistenceException e) {
				log.error(e.getMessage(), e);
				return null;
			}
		}).collect(Collectors.toList());
		guiMenus.sort((m1, m2) -> {
			if (m1.getPosition() == m2.getPosition())
				return m1.getName().compareToIgnoreCase(m2.getName());
			return m1.getPosition() < m2.getPosition() ? -1 : 1;
		});

		return guiMenus.toArray(new GUIMenu[0]);

	}

	@Override
	public GUIMenu getMenu(long menuId, String locale) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		GroupDAO gdao = (GroupDAO) Context.get().getBean(GroupDAO.class);
		MenuDAO dao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		try {
			Menu menu = dao.findById(menuId);
			if (menu == null)
				return null;

			GUIMenu f = toGUIMenu(menu, locale);

			int i = 0;
			GUIRight[] rights = new GUIRight[menu.getMenuGroups().size()];
			for (MenuGroup fg : menu.getMenuGroups()) {
				Group group = gdao.findById(fg.getGroupId());
				if (group == null || group.getTenantId() != session.getTenantId())
					continue;

				GUIRight right = new GUIRight();
				right.setEntityId(fg.getGroupId());
				rights[i] = right;
				i++;
			}
			f.setRights(rights);

			return f;
		} catch (PersistenceException e) {
			return (GUIMenu) throwServerException(session, log, e);
		}

	}

	private GUIMenu toGUIMenu(Menu menu, String locale) throws PersistenceException {
		GUIMenu f = new GUIMenu();
		f.setId(menu.getId());
		f.setName(menu.getName());
		f.setEnabled(menu.getEnabled() == 1);
		f.setAutomation(menu.getAutomation());
		f.setRoutineId(menu.getRoutineId());
		f.setPosition(menu.getPosition());
		f.setDescription(menu.getDescription());
		f.setSecurityRef(menu.getSecurityRef());
		f.setParentId(menu.getParentId());
		f.setType(menu.getType());

		MenuDAO dao = (MenuDAO) Context.get().getBean(MenuDAO.class);
		dao.initialize(menu);

		List<GUIRight> rights = new ArrayList<>();

		if (menu.getMenuGroups() != null && !menu.getMenuGroups().isEmpty()) {
			GroupDAO gdao = (GroupDAO) Context.get().getBean(GroupDAO.class);
			UserDAO udao = (UserDAO) Context.get().getBean(UserDAO.class);
			for (MenuGroup mg : menu.getMenuGroups()) {
				GUIRight right = new GUIRight();
				right.setEntityId(mg.getGroupId());
				right.setDelete(mg.getDelete() == 1);
				right.setSecurity(mg.getManageSecurity() == 1);
				right.setRename(mg.getRename() == 1);
				right.setWrite(mg.getWrite() == 1);

				Group group = gdao.findById(mg.getGroupId());
				if (group == null)
					continue;

				if (group.getType() == Group.TYPE_DEFAULT) {
					right.setLabel(group.getName());
					right.setName(I18N.message("group", LocaleUtil.toLocale(locale)) + ": " + group.getName());
				} else {
					User user = udao.findByGroup(group.getId()).iterator().next();
					right.setLabel(user.getUsername());
					right.setName(I18N.message("user", LocaleUtil.toLocale(locale)) + ": " + user.getFullName() + " ("
							+ user.getUsername() + ")");
				}

				rights.add(right);
			}
		}

		f.setRights(rights.toArray(new GUIRight[0]));

		return f;
	}

	@Override
	public GUIUser[] searchUsers(String username, String groupId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);

		StringBuilder query = new StringBuilder(
				"select A.ld_id, A.ld_username, A.ld_name, A.ld_firstname from ld_user A ");
		if (StringUtils.isNotEmpty(groupId))
			query.append(", ld_usergroup B");
		query.append(" where A.ld_deleted=0 and A.ld_type=" + User.TYPE_DEFAULT);
		if (StringUtils.isNotEmpty(username))
			query.append(" and A.ld_username like '%" + SqlUtil.doubleQuotes(username) + "%'");
		if (StringUtils.isNotEmpty(groupId))
			query.append(" and A.ld_id=B.ld_userid and B.ld_groupid=" + Long.parseLong(groupId));

		try {
			@SuppressWarnings("unchecked")
			List<GUIUser> users = userDao.query(query.toString(), null, new RowMapper<>() {

				@Override
				public GUIUser mapRow(ResultSet rs, int row) throws SQLException {
					GUIUser user = new GUIUser();
					user.setId(rs.getLong(1));
					user.setUsername(rs.getString(2));
					user.setName(rs.getString(3));
					user.setFirstName(rs.getString(4));
					return user;
				}
			}, null);

			return users.toArray(new GUIUser[0]);
		} catch (PersistenceException e) {
			return (GUIUser[]) throwServerException(session, log, e);
		}

	}

	@Override
	public GUISequence[] loadBlockedEntities() throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		if (session.getTenantId() != Tenant.DEFAULT_ID)
			return new GUISequence[0];

		ContextProperties config = Context.get().getProperties();
		SequenceDAO dao = (SequenceDAO) Context.get().getBean(SequenceDAO.class);
		List<Sequence> seqs = new ArrayList<>();
		long max = config.getInt("throttle.username.max", 0);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -config.getInt("throttle.username.wait", 0));
		Date oldestDate = cal.getTime();

		Map<String, Object> params = new HashMap<>();
		params.put("oldestDate", oldestDate);
		params.put("max", max);

		try {
			if (max > 0)
				seqs.addAll(dao.findByWhere(
						"_entity.name like '" + LoginThrottle.LOGINFAIL_USERNAME
								+ "%' and _entity.value >= :max and _entity.lastModified >= :oldestDate",
						params, null, null));

			max = config.getInt("throttle.ip.max", 0);
			cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -config.getInt("throttle.ip.wait", 0));
			if (max > 0)
				seqs.addAll(dao.findByWhere(
						"_entity.name like '" + LoginThrottle.LOGINFAIL_IP
								+ "%' and _entity.value >= :max and _entity.lastModified >= :oldestDate",
						params, null, null));

			GUISequence[] ret = new GUISequence[seqs.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = new GUISequence();
				ret[i].setId(seqs.get(i).getId());
				ret[i].setValue(seqs.get(i).getValue());
				ret[i].setLastModified(seqs.get(i).getLastModified());
				ret[i].setName(seqs.get(i).getName());
			}

			return ret;
		} catch (PersistenceException e) {
			return (GUISequence[]) throwServerException(session, log, e);
		}

	}

	@Override
	public void removeBlockedEntities(long[] ids) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		if (session.getTenantId() != Tenant.DEFAULT_ID)
			return;

		SequenceDAO dao = (SequenceDAO) Context.get().getBean(SequenceDAO.class);
		try {
			for (long id : ids) {
				dao.delete(id);
			}
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public void replicateUsersSettings(long masterUserId, Long[] userIds, boolean gui, boolean groups)
			throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		try {
			User masterUser = userDao.findById(masterUserId);
			userDao.initialize(masterUser);
			Set<Group> masterGroups = masterUser.getGroups();

			for (Long userId : userIds) {
				User user = userDao.findById(userId);
				userDao.initialize(user);

				if (gui) {
					user.setDefaultWorkspace(masterUser.getDefaultWorkspace());
					user.setWelcomeScreen(masterUser.getWelcomeScreen());
					user.setDocsGrid(masterUser.getDocsGrid());
					user.setHitsGrid(masterUser.getHitsGrid());
				}

				if (groups && !user.isReadonly()) {
					user.removeGroupMemberships(null);
					for (Group grp : masterGroups) {
						if (!grp.isUserGroup())
							user.addGroup(grp);
					}
				}

				UserHistory transaction = new UserHistory();
				transaction.setSession(session);
				transaction.setComment(String.format("Settings replicated from %s", masterUser.getUsername()));
				userDao.store(user, transaction);

				log.info("Replicated the settings to user {}", user.getName());
			}
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public void updateDeviceLabel(long deviceId, String label) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		DeviceDAO dDao = (DeviceDAO) Context.get().getBean(DeviceDAO.class);
		try {
			Device device = dDao.findById(deviceId);
			if (device != null) {
				device.setLabel(StringUtils.isNotEmpty(label) ? label.trim() : null);
				dDao.store(device);
			}
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}

	}

	@Override
	public String trustDevice(String label) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		if (session.getClient() != null && session.getClient().getDevice() != null) {
			Device device = session.getClient().getDevice();
			if (label != null)
				device.setLabel(label);
			DeviceDAO dDao = (DeviceDAO) Context.get().getBean(DeviceDAO.class);
			try {
				device = dDao.trustDevice(session.getUser(), device);
			} catch (PersistenceException e) {
				return (String) throwServerException(session, log, e);
			}
			return device.getDeviceId();
		} else
			return null;

	}

	@Override
	public Boolean isTrustedDevice(String deviceId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		// If the second factor is not enabled on the user, the device is
		// always trusted
		if (StringUtils.isEmpty(session.getUser().getSecondFactor()))
			return true;

		DeviceDAO dDao = (DeviceDAO) Context.get().getBean(DeviceDAO.class);
		Device device = dDao.findByDeviceId(deviceId);
		return device != null && device.getUserId() == session.getUserId() && device.getTrusted() == 1;

	}

	@Override
	public void deleteTrustedDevices(String[] ids) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		if (ids == null || ids.length < 1)
			return;

		DeviceDAO dDao = (DeviceDAO) Context.get().getBean(DeviceDAO.class);
		for (String id : ids)
			try {
				dDao.delete(Long.parseLong(id));
			} catch (NumberFormatException | PersistenceException e) {
				throwServerException(session, log, e);
			}
	}

	@Override
	public String syncGeolocationDB(String key) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());
		Context.get().getProperties().setProperty(SECURITY_GEOLOCATION_APIKEY, key != null ? key : "");
		try {
			Context.get().getProperties().write();

			Geolocation.get().syncDB(key);
			return Geolocation.get().getDatabaseVersion();
		} catch (IOException e) {
			return (String) throwServerException(session, log, e);
		}

	}

	@Override
	public void saveAvatar(long userId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		Map<String, File> uploadedFilesMap = UploadServlet.getReceivedFiles(session.getSid());
		File file = uploadedFilesMap.values().iterator().next();
		try {
			UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
			User user = userDao.findById(userId);
			if (user != null)
				UserUtil.saveAvatar(user, file);
		} catch (PersistenceException e) {
			log.error("Unable to store the avatar", e);
		} finally {
			UploadServlet.cleanReceivedFiles(session.getSid());
		}
	}

	@Override
	public void resetAvatar(long userId) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		try {
			UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
			User user = userDao.findById(userId);
			if (user != null)
				UserUtil.generateDefaultAvatar(user);
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}

	}

	@Override
	public void cloneWorkTimes(long srcUserId, long[] userIds, long[] groupIds) throws ServerException {
		Session session = validateSession(getThreadLocalRequest());

		Set<Long> uniqueUserIds = Arrays.stream(userIds).boxed().distinct().collect(Collectors.toSet());

		if (groupIds != null) {
			UserDAO gDao = (UserDAO) Context.get().getBean(UserDAO.class);

			Arrays.stream(groupIds).forEach(gId -> {
				Set<User> usrs = gDao.findByGroup(gId);
				for (User user : usrs) {
					if (!uniqueUserIds.contains(user.getId()))
						uniqueUserIds.add(user.getId());
				}
			});
		}

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		try {
			User srcUser = userDao.findById(srcUserId);
			userDao.initialize(srcUser);

			for (Long userId : uniqueUserIds) {
				User user = userDao.findById(userId);
				userDao.initialize(user);
				if (srcUser.getWorkingTimes() != null)
					for (WorkingTime wt : srcUser.getWorkingTimes())
						user.getWorkingTimes().add(new WorkingTime(wt));
				UserHistory transaction = new UserHistory();
				transaction.setSession(session);
				transaction.setEvent(UserEvent.UPDATED.toString());
				userDao.store(user, transaction);
			}
		} catch (PersistenceException e) {
			throwServerException(session, log, e);
		}

	}

	@Override
	public void changeStatus(long userId, boolean enabled) throws ServerException {
		checkMenu(getThreadLocalRequest(), Menu.SECURITY);
		Session session = checkMenu(getThreadLocalRequest(), Menu.SECURITY);

		UserDAO userDao = (UserDAO) Context.get().getBean(UserDAO.class);
		try {
			User user = userDao.findById(userId, true);
			if (user == null)
				throw new ServerException(String.format("User %s not found", userId));

			if (!enabled && ADMIN.equals(user.getUsername()))
				throw new ServerException("Cannot diable the admin user");

			UserHistory transaction = new UserHistory();
			transaction.setSession(session);
			transaction.setEvent(enabled ? UserEvent.UPDATED.toString() : UserEvent.DISABLED.toString());

			user.setEnabled(enabled ? 1 : 0);
			userDao.store(user, transaction);
		} catch (PersistenceException | ServerException e) {
			throwServerException(session, log, e);
		}
	}

	@Override
	public String generatePassword() throws InvalidSessionServerException {
		Session session = validateSession(getThreadLocalRequest());
		String tenant = session.getTenantName();

		// Generate an initial password(that must be changed)
		ContextProperties config = Context.get().getProperties();
		return PasswordGenerator.generate(config.getInt(tenant + PASSWORD_SIZE, 8),
				config.getInt(tenant + PASSWORD_UPPERCASE, 2), config.getInt(tenant + PASSWORD_LOWERCASE, 2),
				config.getInt(tenant + PASSWORD_DIGIT, 1), config.getInt(tenant + PASSWORD_SPECIAL, 1),
				config.getInt(tenant + PASSWORD_SEQUENCE, 4), config.getInt(tenant + PASSWORD_OCCURRENCE, 3));
	}

	@Override
	public String generatePassword2(int length, int uppercaseChars, int lowercaseChars, int digits, int specialChars,
			int maxSequenceSize, int maxOccurrences) {
		return PasswordGenerator.generate(length, uppercaseChars, lowercaseChars, digits, specialChars, maxSequenceSize,
				maxOccurrences);
	}
}